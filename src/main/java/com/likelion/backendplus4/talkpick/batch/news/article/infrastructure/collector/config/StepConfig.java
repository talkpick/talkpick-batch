package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
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
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.rss.RssSource;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;

@Configuration
public class StepConfig {
	private final JobRepository jobRepository;
	private final Partitioner rssSourcePartitioner;
	private final PlatformTransactionManager transactionManager;
	private final TaskExecutor taskExecutor;
	private final ItemProcessor<RssSource, List<ArticleEntity>> processor;
	private final ItemWriter<List<ArticleEntity>> writer;
	private final int gridSize = 1;
	private static final String executorName = "normalExecutor";

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
	public Job rssJob(JobRepository jobRepository, Step rssPartitionedStep) {
		return new JobBuilder("rssJob", jobRepository)
			.start(rssPartitionedStep)
			.build();
	}

	@Bean
	public Step rssPartitionedStep(Step rssStep) {

		return new StepBuilder("rssPartitionedStep", jobRepository)
			.partitioner(rssStep.getName(), rssSourcePartitioner)
			.step(rssStep)
			.taskExecutor(taskExecutor)
			.gridSize(gridSize)
			.build();
	}

	@Bean
	public Step rssStep(ItemReader<RssSource> articleReader) {

		return new StepBuilder("rssStep", jobRepository)
			.<RssSource, List<ArticleEntity>>chunk(100, transactionManager)
			.reader(articleReader)
			.processor(processor)
			.writer(writer)
			.faultTolerant()
			.skip(ArticleCollectorException.class)
			.skipLimit(100)
			.build();
	}
}
