package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.summary.batch.writer;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.repository.NewsInfoJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ArticleSummaryWriter implements ItemWriter<ArticleEntity> {
	private final NewsInfoJpaRepository newsInfoJpaRepository;
	@Override
	public void write(Chunk<? extends ArticleEntity> chunk) throws Exception {
		newsInfoJpaRepository.saveAll(chunk);
	}
}
