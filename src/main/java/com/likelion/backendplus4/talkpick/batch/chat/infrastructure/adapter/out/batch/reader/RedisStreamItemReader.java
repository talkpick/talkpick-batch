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
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
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
	private final List<MapRecord<String, String, String>> pendingToAck = new ArrayList<>();

	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		this.buffer = null;
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

	/**
	 * 읽어들인 레코드를 ACK 하여 재처리를 방지한다.
	 *
	 * @since 2025-05-26
	 */
	public void ackPending() {
		if (pendingToAck.isEmpty())
			return;
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

	private List<MapRecord<String, String, String>> fetchRecords() {
		Set<String> keys = streamKeys();
		if (keys.isEmpty()) {
			return List.of();
		}
		keys.forEach(this::ensureGroup);

		List<StreamOffset<String>> offsets = new ArrayList<>(keys.size());
		keys.forEach(k -> offsets.add(StreamOffset.create(k, ReadOffset.lastConsumed())));

		StreamReadOptions opts = StreamReadOptions.empty()
			.count((long)BATCH_SIZE * keys.size())
			.block(Duration.ofMillis(blockMillis / 2));

		List<MapRecord<String, String, String>> result = getMapRecords(opts, offsets);
		return result == null ? List.of() : result;
	}

	private List<MapRecord<String, String, String>> getMapRecords(StreamReadOptions opts,
		List<StreamOffset<String>> offsets) {
		return redisTemplate.opsForStream().read(Consumer.from(GROUP, CONSUMER),
			opts,
			offsets.toArray(new StreamOffset[0])
		);
	}

	private Set<String> streamKeys() {
		return redisTemplate.keys(STREAM_KEY_PREFIX + "*");
	}

	private void ensureGroup(String streamKey) {
		SetOperations<String, String> setOps = redisTemplate.opsForSet();
		Long added = setOps.add(GROUP_SET_KEY, streamKey);

		if (added != null && added == 0L) {
			return;
		}

		createGroupSafe(streamKey);
	}

	/**
	 * 스트림 키에 대해 컨슈머 그룹을 생성합니다. BUSYGROUP 예외는 무시합니다.
	 */
	private void createGroupSafe(String streamKey) {
		try {
			redisTemplate.opsForStream()
				.createGroup(streamKey, ReadOffset.from("0"), GROUP);
		} catch (Exception ex) {
			if (!String.valueOf(ex.getMessage()).contains("BUSYGROUP")) {
				log.error("Failed to create consumer group for stream {}", streamKey, ex);
			}
		}
	}
}