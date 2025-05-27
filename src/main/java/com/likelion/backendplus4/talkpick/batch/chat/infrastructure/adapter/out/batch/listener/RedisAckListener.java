package com.likelion.backendplus4.talkpick.batch.chat.infrastructure.adapter.out.batch.listener;


import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.chat.infrastructure.adapter.out.batch.reader.RedisStreamItemReader;

import lombok.RequiredArgsConstructor;

/**
 * 배치 처리 후에 Redis 스트림 레코드를 ACK 처리하는 ChunkListener입니다.
 *
 * @since 2025-05-27
 */
@Component
@RequiredArgsConstructor
public class RedisAckListener implements ChunkListener {

	private final RedisStreamItemReader reader;

	/**
	 * 청크 처리가 완료된 후 Redis 스트림 레코드를 ACK 처리합니다.
	 *
	 * @param context Chunk 처리 문맥
	 * @author 박찬병
	 * @since 2025-05-27
	 */
	@Override
	public void afterChunk(ChunkContext context) {
		reader.ackPending();
	}
}
