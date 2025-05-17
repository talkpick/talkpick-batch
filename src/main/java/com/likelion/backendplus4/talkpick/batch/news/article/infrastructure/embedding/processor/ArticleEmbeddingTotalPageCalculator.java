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

@Component
@Slf4j
@RequiredArgsConstructor
public class ArticleEmbeddingTotalPageCalculator implements Tasklet {
	private final PagePartitioner pagePartitioner;
	private final NewsInfoJpaRepository newsInfoJpaRepository;

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
