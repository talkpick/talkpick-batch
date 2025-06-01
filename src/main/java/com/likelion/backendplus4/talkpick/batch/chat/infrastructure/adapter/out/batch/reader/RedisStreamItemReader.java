package com.likelion.backendplus4.talkpick.batch.chat.infrastructure.adapter.out.batch.reader;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.chat.exception.ChatBatchException;
import com.likelion.backendplus4.talkpick.batch.chat.exception.error.ChatBatchErrorCode;
import com.likelion.backendplus4.talkpick.batch.common.annotation.logging.EntryExitLog;

import lombok.extern.slf4j.Slf4j;

/**
 * Redis 스트림에서 배치 처리용 레코드를 읽어오는 ItemStreamReader 구현체입니다.
 *
 * @since 2025-05-26
 */
@Slf4j
@Component
public class RedisStreamItemReader implements ItemStreamReader<MapRecord<String, String, String>> {

	private static final String STREAM_KEY_PREFIX = "chat:stream:*";
	private static final String GROUP_SET_KEY = "chat:known-groups";
	private static final String GROUP = "chatGroup";
	private static final String CONSUMER = "batch-reader";

	private final int batchSize;
	private final long blockMillis;
	private final RedisTemplate<String, String> redisTemplate;
	private final StreamOperations<String, String, String> streamOperations;
	private final List<MapRecord<String, String, String>> pendingToAck = new ArrayList<>();
	private int pendingReadIndex = 0;

	private Iterator<MapRecord<String, String, String>> buffer;;
	private Set<String> currentStreamKeys;

	public RedisStreamItemReader(
		RedisTemplate<String, String> redisTemplate,
		@Value("${chat.flush.interval}") long blockMillis,
		@Value("${chat.flush.batch-size}") int batchSize) {
		this.redisTemplate = redisTemplate;
		this.streamOperations = redisTemplate.opsForStream();
		this.blockMillis = blockMillis;
		this.batchSize = batchSize;
	}


	/**
	 * Reader 상태를 초기화합니다.
	 *
	 * 1. 이전 배치 실행에서 남아 있을 수 있는 {@code buffer} iterator를 {@code null}로 설정합니다.
	 * 2. 다음 {@code read()} 호출 시 새로 레코드를 조회하도록 강제합니다.
	 *
	 * @param executionContext 현재 스텝의 ExecutionContext
	 * @throws ItemStreamException 초기화 실패 시
	 * @author 박찬병
	 * @since 2025-05-27
	 */
	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		this.buffer = null;
		this.currentStreamKeys = streamKeys();
		claimOldPendingMessages(this.currentStreamKeys);

	}

	/**
	 * Reader가 종료될 때 호출되어 내부 상태를 정리합니다.
	 *
	 * @throws ItemStreamException 종료 처리 중 예외 발생 시
	 * @author 박찬병
	 * @since 2025-05-27
	 */
	@Override
	public void close() throws ItemStreamException {
		pendingToAck.clear();
		pendingReadIndex = 0;
	}

	/**
	 * Redis 스트림에서 레코드를 순차적으로 읽어옵니다.
	 *
	 * 1. {@code buffer}가 비어있거나 더 이상 요소가 없으면 {@link #fetchRecords(Set)}를 호출하여 새 레코드 묶음을 가져옵니다.
	 * 2. 가져온 레코드가 없으면 {@code null}을 반환하여 Step이 종료되도록 합니다.
	 * 3. 레코드가 존재하면 {@code pendingToAck}에 추가하고 {@code buffer}를 새 iterator로 초기화합니다.
	 * 4. {@code buffer.next()}를 호출해 다음 레코드를 반환합니다.
	 *
	 * @return 다음 MapRecord, 더 이상 없으면 {@code null}
	 * @author 박찬병
	 * @since 2025-05-27
	 */
	@Override
	public MapRecord<String, String, String> read() {
		if (buffer == null || !buffer.hasNext()) {
			List<MapRecord<String, String, String>> recs = fetchRecords(currentStreamKeys);
			log.info("읽어온 recs = {}", recs.size());
			if (recs.isEmpty()) {
				return null;
			}
			pendingToAck.addAll(recs);
			buffer = recs.iterator();
		}
		return buffer.next();
	}

	/**
	 * 읽어들인 레코드들을 일괄 ACK 처리합니다.
	 *
	 * 1. {@code pendingToAck}가 비어있으면 즉시 반환합니다.
	 * 2. 스트림별로 레코드를 그룹화하여 동일 스트림에 대해 한 번의 호출로 ACK 합니다.
	 * 3. ACK 후 {@code pendingToAck} 리스트를 비워 재처리를 방지합니다.
	 *
	 * @author 박찬병
	 * @since 2025-05-26
	 */
	public void ackPending() {
		if (pendingToAck.isEmpty()) {
			return;
		}
		pendingToAck.stream()
			.collect(Collectors.groupingBy(MapRecord::getStream))
			.forEach((streamKey, recList) -> {
				RecordId[] ids = recList.stream()
					.map(MapRecord::getId)
					.toArray(RecordId[]::new);
				streamOperations.acknowledge(streamKey, GROUP, ids);
			});
		// After acknowledging all pendingToAck entries, clear the list and reset index
		pendingToAck.clear();
	}

	/**
	 * 사용 가능한 Redis 스트림 키들을 조회합니다.
	 *
	 * @return 스트림 키 세트
	 * @author 박찬병
	 * @since 2025-05-27
	 */
	private Set<String> streamKeys() {
		return redisTemplate.keys(STREAM_KEY_PREFIX);
	}


	@EntryExitLog
	private void claimOldPendingMessages(Set<String> keys) {
		if (keys.isEmpty()) {
			return;
		}

		for (String streamKey : keys) {
			try {
				PendingMessages pending = streamOperations
					.pending(streamKey, Consumer.from(GROUP, CONSUMER), Range.unbounded(), 100L);

				List<RecordId> idsList = new ArrayList<>();
				for (PendingMessage pm : pending) {
					idsList.add(pm.getId());
				}
				RecordId[] ids = idsList.toArray(new RecordId[0]);

				if (ids.length > 0) {
					List<MapRecord<String, String, String>> claimedRaw = streamOperations
						.claim(streamKey, GROUP, CONSUMER, Duration.ZERO, ids);
					if (!claimedRaw.isEmpty()) {
						pendingToAck.addAll(claimedRaw);
						buffer = claimedRaw.iterator();
					}
				}
			} catch (Exception e) {
				log.warn("Pending 메시지 클레임 실패: streamKey={}, error={}", streamKey, e.getMessage());
			}
		}
	}

	/**
	 * 활성 스트림 키에서 새 레코드 묶음을 조회합니다.
	 *
	 * 1. {@link #streamKeys()}로 스트림 키 세트를 가져옵니다.
	 * 2. 키가 없으면 빈 리스트를 반환합니다.
	 * 3. 각 키에 대해 {@link #ensureGroup(String)}로 컨슈머 그룹을 보장합니다.
	 * 4. 모든 키를 대상으로 마지막으로 소비한 이후 항목부터 읽도록 {@code offsets}를 구성합니다.
	 * 5. {@code blockMillis}의 절반 동안 블로킹하며 최대 {@code batchSize * keys.size()} 만큼 읽습니다.
	 * 6. 읽어온 레코드 리스트를 반환합니다.
	 *
	 * @return 조회된 MapRecord 리스트, 없으면 빈 리스트
	 * @author 박찬병
	 * @since 2025-05-27
	 */
	private List<MapRecord<String, String, String>> fetchRecords(Set<String> keys) {
		if (keys.isEmpty()) {
			return List.of();
		}
		keys.forEach(this::ensureGroup);

		List<StreamOffset<String>> offsets = new ArrayList<>(keys.size());
		keys.forEach(k -> offsets.add(StreamOffset.create(k, ReadOffset.lastConsumed())));

		StreamReadOptions opts = StreamReadOptions.empty()
			.count((long) batchSize * keys.size())
			.block(Duration.ofMillis(blockMillis / 2));

		return getMapRecords(opts, offsets);
	}

	/**
	 * 주어진 StreamReadOptions와 offsets로부터 MapRecord를 읽어 반환합니다.
	 *
	 * @param opts    스트림 읽기 옵션
	 * @param offsets 읽기를 수행할 StreamOffset 리스트
	 * @return 읽어온 MapRecord 리스트
	 * @author 박찬병
	 * @since 2025-05-27
	 */
	private List<MapRecord<String, String, String>> getMapRecords(StreamReadOptions opts,
		List<StreamOffset<String>> offsets) {
		return streamOperations.read(Consumer.from(GROUP, CONSUMER),
			opts,
			offsets.toArray(new StreamOffset[0])
		);
	}

	/**
	 * 주어진 스트림 키에 대한 컨슈머 그룹을 보장합니다.
	 *
	 * 1. Redis SET({@code chat:known-groups})을 통해 이미 그룹을 생성한 스트림인지 확인합니다.
	 * 2. 처음 보는 스트림이면 {@link #createGroupSafe(String)}를 호출해 그룹을 생성합니다.
	 * 3. 이미 존재한다면 아무 작업도 수행하지 않습니다.
	 *
	 * @param streamKey 대상 스트림 키
	 * @author 박찬병
	 * @since 2025-05-27
	 */
	private void ensureGroup(String streamKey) {
		SetOperations<String, String> setOps = redisTemplate.opsForSet();
		Long added = setOps.add(GROUP_SET_KEY, streamKey);

		if (null != added && 0L == added) {
			return;
		}

		createGroupSafe(streamKey);
	}

	/**
	 * 스트림 키에 대해 컨슈머 그룹을 생성합니다. BUSYGROUP 예외는 무시합니다.
	 *
	 * @param streamKey 대상 스트림 키
	 * @author 박찬병
	 * @since 2025-05-27
	 */
	private void createGroupSafe(String streamKey) {
		try {
			streamOperations.createGroup(streamKey, ReadOffset.from("0"), GROUP);
		} catch (Exception ex) {
			if (String.valueOf(ex.getMessage()).contains("BUSYGROUP")) {
				return;
			}
			log.error("스트림 {}에 대한 컨슈머 그룹 생성에 실패했습니다.", streamKey, ex);
			throw new ChatBatchException(ChatBatchErrorCode.REDIS_GROUP_CREATE_FAILED, ex);
		}
	}


}