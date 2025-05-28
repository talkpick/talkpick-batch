package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.batch.step.writer;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.jpa.entity.ArticleEntity;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.jpa.repository.NewsInfoJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ArticleWriter implements ItemWriter<ArticleEntity> {
	private final NewsInfoJpaRepository newsInfoJpaRepository;

	@Override
	public void write(Chunk<? extends ArticleEntity> chunk) {
		newsInfoJpaRepository.saveAll(chunk);
	}
}
