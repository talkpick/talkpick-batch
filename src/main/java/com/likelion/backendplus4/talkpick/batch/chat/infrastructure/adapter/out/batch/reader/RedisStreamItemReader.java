package com.likelion.backendplus4.talkpick.batch.chat.infrastructure.adapter.out.batch.reader;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.batch.item.ItemStreamReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisStreamItemReader implements ItemStreamReader<MapRecord<String, String, String>> {

	private final RedisTemplate<String, String> redisTemplate;

	@Value("${chat.flush.delay}")
	private long blockMillis;

	private static final String STREAM_KEY_PREFIX = "chat:stream:";
	private static final String GROUP = "chatGroup";
	private static final String CONSUMER = "batch-reader";
	private static final int BATCH_SIZE = 100;

	private Iterator<MapRecord<String, String, String>> iterator;

	@Override
	public MapRecord<String, String, String> read() {
		if (iterator != null && iterator.hasNext()) {
			return iterator.next();
		}
		Set<String> keys = redisTemplate.keys(STREAM_KEY_PREFIX + "*");
		if (keys.isEmpty()) {
			return null;
		}
		StreamOperations<String, String, String> ops = redisTemplate.opsForStream();
		List<MapRecord<String, String, String>> recs = keys.stream()
			.flatMap(key -> Objects.requireNonNull(ops.read(
				Consumer.from(GROUP, CONSUMER),
				StreamReadOptions.empty()
					.count(BATCH_SIZE)
					.block(Duration.ofMillis(blockMillis / 2)),
				StreamOffset.create(key, ReadOffset.lastConsumed())
			)).stream())
			.toList();

		if (recs.isEmpty()) {
			return null;
		}
		iterator = recs.iterator();
		return iterator.next();
	}

}