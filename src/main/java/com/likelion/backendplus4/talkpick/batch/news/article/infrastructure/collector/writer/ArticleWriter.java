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

/**
 * 기사 데이터를 DB에 저장하는 Spring Batch ItemWriter 구현체.
 * 중복된 링크는 저장하지 않으며, 새롭게 저장된 기사 수를 로그로 출력한다.
 *
 * - 입력: 기사 리스트(List<ArticleEntity>)
 * - 처리: 중복 여부 확인 후 저장
 * - 출력: 로그 출력 (중복 제외)
 *
 * @since 2025-05-10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleWriter implements ItemWriter<List<ArticleEntity>> {

	private final RssNewsRepository rssNewsRepository;

	/**
	 * 기사 리스트를 저장하며, 중복된 기사는 건너뛴다.
	 * 저장 성공 시 개수를 집계하고, 로그로 남긴다.
	 *
	 * @param chunk Spring Batch가 전달하는 기사 리스트 Chunk
	 * @since 2025-05-10
	 * @author 함예정
	 */
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
		logIfNewArticlesFound(savedCount);

	}

	/**
	 * 새로 저장된 기사가 존재하면 개수를 info 로그로 출력한다.
	 *
	 * @param savedCount 저장된 기사 수 (AtomicInteger)
	 * @since 2025-05-10
	 * @author 함예정
	 */
	private void logIfNewArticlesFound(AtomicInteger savedCount) {
		int newArticleCount = savedCount.get();
		if(newArticleCount > 0){
			log.info("새로 저장된 뉴스 개수: {}", savedCount.get());
		}
	}
}