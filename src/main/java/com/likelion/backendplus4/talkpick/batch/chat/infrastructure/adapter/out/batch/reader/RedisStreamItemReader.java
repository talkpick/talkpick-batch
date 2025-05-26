package com.likelion.backendplus4.talkpick.batch.chat.infrastructure.adapter.out.batch.reader;
import com.likelion.backendplus4.talkpick.batch.chat.exception.ChatBatchException;
import com.likelion.backendplus4.talkpick.batch.chat.exception.error.ChatBatchErrorCode;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Collections;
import java.util.stream.Stream;

import org.springframework.batch.item.ItemStreamReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Redis 스트림에서 배치 처리용 레코드를 읽어오는 ItemStreamReader 구현체입니다.
 *
 * @since 2025-05-26
 */
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

	/**
	 * Redis 스트림으로부터 레코드를 한 개씩 읽어 반환합니다.
	 *
	 * @return 읽은 MapRecord 객체, 더 이상 읽을 레코드가 없으면 null
	 * @since 2025-05-26
	 * @author 박찬병
	 */
	@Override
	public MapRecord<String, String, String> read() {
		List<MapRecord<String, String, String>> recs = fetchRecords();
		if (recs.isEmpty()) {
			return null;
		}

		MapRecord<String, String, String> record = recs.getFirst();
		acknowledge(record.getStream(), List.of(record));

		return record;
	}

	/**
	 * 전체 스트림 키에 대해 보류 중인 레코드와 신규 레코드를 조회합니다.
	 *
	 * @return 모든 키의 레코드를 합친 List
	 * @since 2025-05-26
	 * @author 박찬병
	 */
	private List<MapRecord<String, String, String>> fetchRecords() {
		Set<String> keys = getStreamKeys();
		return keys.stream()
			.flatMap(this::fetchRecordsForKey)
			.toList();
	}

    /**
     * 스트림 키 목록을 조회합니다.
     *
     * @return 스트림 키들의 Set
     * @since 2025-05-26
     * @author 박찬병
     */
    private Set<String> getStreamKeys() {
        return redisTemplate.keys(STREAM_KEY_PREFIX + "*");
    }


    /**
     * 단일 키에 대해 보류 중인 레코드와 신규 레코드를 스트림 형태로 결합하여 반환합니다.
     *
     * @param key 처리할 Redis 스트림 키
     * @return 해당 키의 레코드 스트림
     * @since 2025-05-26
     * @author 박찬병
     */
    private Stream<MapRecord<String, String, String>> fetchRecordsForKey(String key) {
        return Stream.concat(
            fetchPendingRecordsForKey(key).stream(),
            fetchNewRecordsForKey(key).stream()
        );
    }

	/**
	 * 주어진 키에 대한 보류(PENDING) 레코드를 조회합니다.
	 *
	 * @param key Redis 스트림 키
	 * @return 보류 중인 레코드들의 List
	 * @since 2025-05-26
	 * @author 박찬병
	 */
	private List<MapRecord<String, String, String>> fetchPendingRecordsForKey(String key) {
	    StreamOperations<String, String, String> ops = redisTemplate.opsForStream();
	    return Objects.requireNonNull(ops.read(
			Consumer.from(GROUP, CONSUMER),
			StreamReadOptions.empty().count(BATCH_SIZE),
			StreamOffset.create(key, ReadOffset.from("0"))
		)).stream().toList();
	}

	/**
	 * 주어진 키에서 신규 레코드를 조회합니다.
	 *
	 * @param key Redis 스트림 키
	 * @return 신규 레코드들의 List
	 * @since 2025-05-26
	 * @author 박찬병
	 */
	private List<MapRecord<String, String, String>> fetchNewRecordsForKey(String key) {
		StreamOperations<String, String, String> ops = redisTemplate.opsForStream();
		return Objects.requireNonNull(
			ops.read(
				Consumer.from(GROUP, CONSUMER),
				StreamReadOptions.empty()
					.count(BATCH_SIZE)
					.block(Duration.ofMillis(blockMillis / 2)),
				StreamOffset.create(key, ReadOffset.lastConsumed())
			)).stream().toList();
	}


	/**
	 * 읽어들인 레코드를 ACK 하여 재처리를 방지한다.
	 *
	 * @param key     스트림 키
	 * @param records 처리 완료된 레코드
	 * @since 2025-05-26
	 * @author 박찬병
	 */
	private void acknowledge(String key, List<MapRecord<String, String, String>> records) {
		RecordId[] ids = records.stream()
			.map(MapRecord::getId)
			.toArray(RecordId[]::new);
		redisTemplate.opsForStream().acknowledge(key, GROUP, ids);
	}

}