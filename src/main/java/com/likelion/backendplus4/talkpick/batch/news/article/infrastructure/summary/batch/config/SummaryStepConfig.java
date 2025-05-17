package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.summary.batch.config;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.summary.batch.partitioner.ArticleSummaryPartitioner;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.summary.batch.processor.ArticleSummaryProcessor;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.summary.batch.reader.ArticleSummaryPageReader;

/**
 * 배치 작업에서 기사 요약 처리를 위한 파티셔닝 및 슬레이브 Step을 설정하는 구성 클래스.
 *
 * @since 2025-05-17
 */
@Configuration
public class SummaryStepConfig {
	private static final String partitionedStepName = "articleSummaryStep";
	private final String executorName = "normalExecutor";
	private final String summaryStepName = "articleSummarySlaveStep";
	private final int gridSize = 5;
	private final int chunkSize = 100;
	private final int retryLimit = 3;
	private final int skipLimit = 100;
	private final JobRepository jobRepository;
	private final Partitioner partitioner;
	private final PlatformTransactionManager transactionManager;
	private final TaskExecutor taskExecutor;
	private final ArticleSummaryProcessor summaryProcessor;
	private final ItemWriter<ArticleEntity> writer;

	public SummaryStepConfig(JobRepository jobRepository,
		ArticleSummaryPartitioner partitioner,
		PlatformTransactionManager platformTransactionManager,
		@Qualifier(executorName)
		TaskExecutor taskExecutor,
		ArticleSummaryProcessor summaryProcessor,
		ItemWriter<ArticleEntity> articleSummaryWriter) {
		this.jobRepository = jobRepository;
		this.partitioner = partitioner;
		this.transactionManager = platformTransactionManager;
		this.taskExecutor = taskExecutor;
		this.summaryProcessor = summaryProcessor;
		this.writer = articleSummaryWriter;
	}


	/**
	 * 파티셔닝된 마스터 Step을 정의한다.
	 * 각 파티션은 {@code articleSummarySlaveStep}을 실행하며, 병렬 처리를 위해 TaskExecutor가 사용된다.
	 *
	 * @param articleSummarySlaveStep 파티션마다 실행될 슬레이브 Step
	 * @return 마스터 Step Bean
	 * @author 함예정
	 * @since 2025-05-17
	 */
	@Bean
	public Step articleSummaryStep(Step articleSummarySlaveStep) {
		return new StepBuilder(partitionedStepName, jobRepository)
			.partitioner(partitionedStepName, partitioner)
			.step(articleSummarySlaveStep)
			.taskExecutor(taskExecutor)
			.gridSize(gridSize)
			.build();
	}

	/**
	 * 기사 데이터를 요약 처리하는 슬레이브 Step을 정의한다.
	 * 청크 기반으로 데이터를 읽고, 처리하고, 쓰며, 오류에 대해 재시도 및 건너뛰기를 허용한다.
	 *
	 * @param reader 기사 데이터를 읽는 Reader
	 * @return 슬레이브 Step Bean
	 * @author 함예정
	 * @since 2025-05-17
	 */
	@Bean
	public Step articleSummarySlaveStep(ArticleSummaryPageReader reader) {
		return new StepBuilder(summaryStepName, jobRepository)
			.<ArticleEntity, ArticleEntity>chunk(chunkSize, transactionManager)
			.reader(reader)
			.processor(summaryProcessor)
			.writer(writer)
			.faultTolerant()
			.retry(ArticleCollectorException.class)
			.retryLimit(retryLimit)
			.skip(ArticleCollectorException.class)
			.skipLimit(skipLimit)
			.build();
	}
}
