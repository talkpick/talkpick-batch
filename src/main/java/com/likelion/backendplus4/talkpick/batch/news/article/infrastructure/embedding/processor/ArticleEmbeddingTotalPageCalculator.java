package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.embedding.processor;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.batch.support.PagePartitioner;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.repository.NewsInfoJpaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 임베딩 대상 뉴스 기사(summary가 존재하고 summaryVector가 없는 기사)의
 * 총 페이지 수를 계산하여 PagePartitioner에 설정하는 Tasklet 구현체.
 * Spring Batch에서 Job 실행 시 사전 설정 단계로 활용된다.
 *
 * @since 2025-05-17
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ArticleEmbeddingTotalPageCalculator implements Tasklet {
	private final PagePartitioner pagePartitioner;
	private final NewsInfoJpaRepository newsInfoJpaRepository;

	/**
	 * summary는 존재하지만 summaryVector는 없는 ArticleEntity의 총 페이지 수를 계산하고,
	 * PagePartitioner에 설정한다. 대상 데이터가 없을 경우 ExitStatus를 "ITEMS_NOT_FOUND"로 설정한다.
	 *
	 * @param contribution StepContribution 객체
	 * @param chunkContext ChunkContext 객체
	 * @return RepeatStatus.FINISHED 항상 완료 상태 반환
	 * @author 함예정
	 * @since 2025-05-17
	 */
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

		Pageable pageable = PageRequest.of(0, 100);
		int totalPages = newsInfoJpaRepository
			.findBySummaryIsNotNullAndSummaryVectorIsNull(pageable)
			.getTotalPages();
		pagePartitioner.setTotalPages(totalPages);
		log.info("[임베딩-Total-Page-Calculator] 총 페이지 수 계산 완료: " + totalPages);
		if (totalPages == 0) {
			contribution.setExitStatus(new ExitStatus("ITEMS_NOT_FOUND"));
		}
		return RepeatStatus.FINISHED;
	}
}
