package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.batch;

import java.util.List;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.likelion.backendplus4.talkpick.batch.news.article.application.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;

@Configuration
public class StepConfig {
	private final String executorName = "normalExecutor";
	private static final String partitionedStepName = "articleRssPartitionedStep";
	private final String parseRssStepName = "parseRssStep";
	private final int gridSize = 3;
	private final int chunkSize = 2;
	private final int skipLimit = 100;
	private final JobRepository jobRepository;
	private final Partitioner rssSourcePartitioner;
	private final PlatformTransactionManager transactionManager;
	private final TaskExecutor taskExecutor;
	private final ItemProcessor<RssSource, List<ArticleEntity>> processor;
	private final ItemWriter<List<ArticleEntity>> writer;

	public StepConfig(JobRepository jobRepository,
		Partitioner rssSourcePartitioner,
		PlatformTransactionManager platformTransactionManager,
		@Qualifier(executorName)
		TaskExecutor taskExecutor,
		ItemProcessor<RssSource, List<ArticleEntity>> processor,
		ItemWriter<List<ArticleEntity>> writer) {
		this.jobRepository = jobRepository;
		this.rssSourcePartitioner = rssSourcePartitioner;
		this.transactionManager = platformTransactionManager;
		this.taskExecutor = taskExecutor;
		this.processor = processor;
		this.writer = writer;
	}

	@Bean
	public Step articleRssPartitionedStep(Step rssStep) {

		return new StepBuilder(partitionedStepName, jobRepository)
			.partitioner(rssStep.getName(), rssSourcePartitioner)
			.step(rssStep)
			.taskExecutor(taskExecutor)
			.gridSize(gridSize)
			.build();
	}

	@Bean
	public Step parseRssStep(ItemReader<RssSource> articleReader) {

		return new StepBuilder(parseRssStepName, jobRepository)
			.<RssSource, List<ArticleEntity>>chunk(chunkSize, transactionManager)
			.reader(articleReader)
			.processor(processor)
			.writer(writer)
			.faultTolerant()
			.skip(ArticleCollectorException.class)
			.skipLimit(skipLimit)
			.build();
	}
}
