package com.likelion.backendplus4.talkpick.batch.news.article.application.service;

import org.springframework.stereotype.Service;

import com.likelion.backendplus4.talkpick.batch.news.article.application.port.out.NewsEmbedUseCase;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.quartz.EmbeddingPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsEmbedService implements NewsEmbedUseCase {
	private final EmbeddingPort embeddingPort;
	@Override
	public void embedAllNewsInfo() {
		embeddingPort.startEmbedding();
	}
}
