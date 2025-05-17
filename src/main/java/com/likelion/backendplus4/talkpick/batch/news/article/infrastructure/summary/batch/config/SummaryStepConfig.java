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
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.batch.support.PagePartitioner;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.summary.batch.processor.ArticleSummaryProcessor;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.summary.batch.processor.ArticleSummaryTotalPageCalculator;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.summary.batch.reader.ArticleSummaryPageReader;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;

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
		PagePartitioner partitioner,
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

	@Bean
	Step summaryTotalPageCheckStep(JobRepository jobRepository, PlatformTransactionManager txManager,
		ArticleSummaryTotalPageCalculator totalPageCalculator) {
		return new StepBuilder("summaryTotalPageCheckStep", jobRepository)
			.tasklet(totalPageCalculator, txManager)
			.build();
	}

	@Bean
	public Step articleSummaryStep(Step articleSummarySlaveStep) {
		return new StepBuilder(partitionedStepName, jobRepository)
			.partitioner(partitionedStepName, partitioner)
			.step(articleSummarySlaveStep)
			.taskExecutor(taskExecutor)
			.gridSize(gridSize)
			.build();
	}

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
