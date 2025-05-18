package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.embedding.batch.config;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.embedding.batch.exception.EmbeddingException;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.embedding.batch.partitioner.ArticleEmbeddingPartitioner;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.embedding.batch.processor.ArticleEmbeddingProcessor;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.embedding.batch.reader.ArticleEmbeddingPageReader;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.embedding.batch.writer.ArticleEmbeddingWriter;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.converter.exception.JpaConvertorException;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;

/**
 * 뉴스 기사 임베딩 작업을 위한 Spring Batch Step 설정 클래스.
 * - 총 페이지 수 계산을 위한 tasklet step
 * - 마스터-슬레이브 파티셔닝 기반 임베딩 처리 step 구성
 *
 * @since 2025-05-17
 */
@Configuration
public class ArticleEmbeddingStepConfig {
	private final String executorName = "normalExecutor";
	private final int gridSize = 5;
	private final int chunkSize = 100;
	private final int retryLimit = 3;
	private final int skipLimit = 100;

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;
	private final TaskExecutor taskExecutor;

	public ArticleEmbeddingStepConfig(
		JobRepository jobRepository,
		PlatformTransactionManager platformTransactionManager,
		@Qualifier(executorName) TaskExecutor taskExecutor) {
		this.jobRepository = jobRepository;
		this.transactionManager = platformTransactionManager;
		this.taskExecutor = taskExecutor;
	}

	/**
	 * 기사 임베딩 처리를 마스터-슬레이브 구조로 병렬 처리하기 위한 마스터 Step을 생성한다.
	 *
	 * @param partitioner 파티셔닝 전략 구현체
	 * @param articleEmbeddingSlaveStep 실제 데이터 처리를 수행하는 슬레이브 Step
	 * @return 마스터 파티셔닝 Step
	 * @author 함예정
	 * @since 2025-05-17
	 */
	@Bean
	public Step articleEmbeddingStep(
		ArticleEmbeddingPartitioner partitioner,
		Step articleEmbeddingSlaveStep) {

		return new StepBuilder("articleEmbeddingStep", jobRepository)
			.partitioner("articleEmbeddingSlavePart", partitioner)
			.step(articleEmbeddingSlaveStep)
			.taskExecutor(taskExecutor)
			.gridSize(gridSize)
			.build();
	}

	/**
	 * 한 파티션 내에서 요약된 뉴스 내용을 기준으로 임베딩 벡터를 생성하고
	 * DB에 저장하는 슬레이브 Step을 생성한다.
	 * 지정된 예외에 대해 재시도 및 스킵 처리를 통해 장애 허용 처리를 수행한다.
	 *
	 * @param reader 임베딩 대상 뉴스 기사 데이터를 페이지 단위로 읽어오는 Reader
	 * @param processor 뉴스 요약을 임베딩 벡터로 변환하는 Processor
	 * @param writer 임베딩된 결과를 DB에 저장하는 Writer
	 * @return 슬레이브 Step
	 * @author 함예정
	 * @since 2025-05-17
	 */
	@Bean
	public Step articleEmbeddingSlaveStep(
		ArticleEmbeddingPageReader reader,
		ArticleEmbeddingProcessor processor,
		ArticleEmbeddingWriter writer) {

		return new StepBuilder("articleEmbeddingSlaveStep", jobRepository)
			.<ArticleEntity, ArticleEntity>chunk(chunkSize, transactionManager)
			.reader(reader)
			.processor(processor)
			.writer(writer)
			.faultTolerant()
			.retry(EmbeddingException.class)
			.retryLimit(retryLimit)
			.skip(EmbeddingException.class)
			.skip(JpaConvertorException.class)
			.skipLimit(skipLimit)
			.build();
	}
}
