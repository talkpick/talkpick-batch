package com.likelion.backendplus4.talkpick.batch.chat.infrastructure.adapter.out.batch.reader;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis 스트림에서 배치 처리용 레코드를 읽어오는 ItemStreamReader 구현체입니다.
 *
 * @since 2025-05-26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamItemReader implements ItemStreamReader<MapRecord<String, String, String>> {

	private final RedisTemplate<String, String> redisTemplate;

	@Value("${chat.flush.delay}")
	private long blockMillis;

	private static final String STREAM_KEY_PREFIX = "chat:stream:";
	private static final String GROUP_SET_KEY = "chat:known-groups";
	private static final String GROUP = "chatGroup";
	private static final String CONSUMER = "batch-reader";
	private static final int BATCH_SIZE = 100;

	private Iterator<MapRecord<String, String, String>> buffer;
	private final List<MapRecord<String, String, String>> pendingToAck = new java.util.ArrayList<>();
	private final java.util.Set<String> initializedKeys = new java.util.HashSet<>();

	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		// 리더 초기화: 버퍼 및 키 초기화
		this.buffer = null;
		this.initializedKeys.clear();
	}

	@Override
	public MapRecord<String, String, String> read() {
		if (buffer == null || !buffer.hasNext()) {
			List<MapRecord<String, String, String>> recs = fetchRecords();
			log.info("읽어온 recs = {}", recs.size());
			if (recs.isEmpty()) {
				return null;
			}
			pendingToAck.addAll(recs);
			buffer = recs.iterator();
		}
		return buffer.next();
	}

	private List<MapRecord<String, String, String>> fetchRecords() {
		Set<String> keys = streamKeys();
		if (keys.isEmpty()) {
			return List.of();
		}

		// 새로 발견된 스트림마다 그룹 보장
		keys.forEach(this::ensureGroup);

		// 키별로 첫 호출이면 backlog(0)부터, 이후 호출이면 신규만
		return keys.stream()
			.flatMap(key -> {
				if (initializedKeys.contains(key)) {
					return fetchNewRecordsForKey(key).stream();
				} else {
					initializedKeys.add(key);
					return fetchBacklogForKey(key).stream();
				}
			})
			.toList();
	}

	private Set<String> streamKeys() {
		return redisTemplate.keys(STREAM_KEY_PREFIX + "*");
	}

	private List<MapRecord<String, String, String>> fetchBacklogForKey(String key) {
		log.debug("fetchBacklogForKey(): key={}, batchSize={}", key, BATCH_SIZE);
		StreamOperations<String, String, String> ops = redisTemplate.opsForStream();
		List<MapRecord<String, String, String>> result = ops.read(
			Consumer.from(GROUP, CONSUMER),
			StreamReadOptions.empty().count(BATCH_SIZE),
			StreamOffset.create(key, ReadOffset.from(">"))
		);
		log.debug("fetchBacklogForKey(): key={}, fetched pending size={}", key, result != null ? result.size() : 0);
		return result == null ? List.of() : result;
	}

	private List<MapRecord<String, String, String>> fetchNewRecordsForKey(String key) {
		StreamOperations<String, String, String> ops = redisTemplate.opsForStream();

		// 항상 마지막으로 소비된 이후 메시지부터 읽습니다.
		ReadOffset offset = ReadOffset.lastConsumed();
		log.debug("fetchNewRecordsForKey(): key={}, offset={}, batchSize={}, blockMillis={}",
			key, offset, BATCH_SIZE, blockMillis);

		List<MapRecord<String, String, String>> result = ops.read(
			Consumer.from(GROUP, CONSUMER),
			StreamReadOptions.empty()
				.count(BATCH_SIZE)
				.block(Duration.ofMillis(blockMillis / 2)),
			StreamOffset.create(key, offset)
		);
		log.debug("fetchNewRecordsForKey(): key={}, fetched new size={}", key, result != null ? result.size() : 0);
		return result == null ? List.of() : result;
	}

	private void ensureGroup(String streamKey) {
		SetOperations<String, String> setOps = redisTemplate.opsForSet();
		Long added = setOps.add(GROUP_SET_KEY, streamKey);

		// 이미 기록된 경우에도 실제 그룹 존재 여부 확인
		if (added != null && added == 0L && groupExists(streamKey)) {
			return;
		}
		if (groupExists(streamKey)) {
			return;
		}

		try {
			redisTemplate.opsForStream()
				.createGroup(streamKey, ReadOffset.from("0"), GROUP);
			log.debug("Created consumer-group '{}' for stream '{}'", GROUP, streamKey);
		} catch (Exception ex) {
			if (!String.valueOf(ex.getMessage()).contains("BUSYGROUP")) {
				log.error("Failed to create consumer group for stream {}", streamKey, ex);
			}
		}
	}

	private boolean groupExists(String streamKey) {
		try {
			List<org.springframework.data.redis.connection.stream.StreamInfo.XInfoGroup> groups = redisTemplate.opsForStream().groups(streamKey).toList();
			return groups.stream().anyMatch(g -> GROUP.equals(g.groupName()));
		} catch (Exception e) {
			log.info(e.getLocalizedMessage());
			return false;
		}
	}

	/**
	 * 읽어들인 레코드를 ACK 하여 재처리를 방지한다.
	 *
	 * @since 2025-05-26
	 */
	public void ackPending() {
		if (pendingToAck.isEmpty()) return;
		pendingToAck.stream()
			.collect(Collectors.groupingBy(MapRecord::getStream))
			.forEach((streamKey, recList) -> {
				RecordId[] ids = recList.stream()
					.map(MapRecord::getId)
					.toArray(RecordId[]::new);
				redisTemplate.opsForStream().acknowledge(streamKey, GROUP, ids);
			});
		pendingToAck.clear();
	}
}