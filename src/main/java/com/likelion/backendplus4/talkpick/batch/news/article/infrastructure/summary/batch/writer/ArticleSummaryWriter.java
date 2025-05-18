package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.summary.batch.writer;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.repository.NewsInfoJpaRepository;

import lombok.RequiredArgsConstructor;

/**
 * 처리된 뉴스 기사 요약 데이터를 DB에 저장하는 ItemWriter 구현체.
 *
 * @since 2025-05-17
 */
@Component
@RequiredArgsConstructor
public class ArticleSummaryWriter implements ItemWriter<ArticleEntity> {
	private final NewsInfoJpaRepository newsInfoJpaRepository;

	/**
	 * 청크 단위로 받은 기사 요약 데이터를 데이터베이스에 일괄 저장한다.
	 *
	 * @param chunk 요약이 완료된 기사 데이터 목록
	 * @throws Exception 저장 중 발생할 수 있는 예외
	 * @author 함예정
	 * @since 2025-05-17
	 */
	@Override
	public void write(Chunk<? extends ArticleEntity> chunk) throws Exception {
		newsInfoJpaRepository.saveAll(chunk);
	}
}
