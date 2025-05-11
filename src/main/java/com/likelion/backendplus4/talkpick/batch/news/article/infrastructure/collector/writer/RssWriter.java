package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.writer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.repository.RssNewsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RssWriter implements ItemWriter<List<ArticleEntity>> {

	private final RssNewsRepository rssNewsRepository;

	@Override
	public void write(Chunk<? extends List<ArticleEntity>> chunk) {
		AtomicInteger savedCount = new AtomicInteger();
		chunk.getItems().stream()
			.flatMap(List::stream)
			.filter(item -> !rssNewsRepository.existsByLink(item.getLink()))
			.forEach(item -> {
				try {
					rssNewsRepository.save(item);
					savedCount.incrementAndGet();
				} catch (DataIntegrityViolationException e) {
					log.debug("중복 항목 감지: {}", item.getLink());
				}
			});

		log.info("새로 저장된 뉴스 개수: {}", savedCount.get());
	}
}