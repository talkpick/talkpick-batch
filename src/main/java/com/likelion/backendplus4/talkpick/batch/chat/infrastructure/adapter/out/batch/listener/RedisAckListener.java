package com.likelion.backendplus4.talkpick.batch.chat.infrastructure.adapter.out.batch.listener;


import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.chat.infrastructure.adapter.out.batch.reader.RedisStreamItemReader;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisAckListener implements ChunkListener {

	private final RedisStreamItemReader reader;


	@Override
	public void afterChunk(ChunkContext context) {
		reader.ackPending();
	}
}
