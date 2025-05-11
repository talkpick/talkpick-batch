package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobConfig {
	private final String jobName = "articleCollectorBatchJob";
	private final JobRepository jobRepository;
	private final Step rssPartitionedStep;

	public JobConfig(JobRepository jobRepository, Step articleRssPartitionedStep) {
		this.jobRepository = jobRepository;
		this.rssPartitionedStep = articleRssPartitionedStep;
	}

	@Bean
	public Job articleCollectJob() {
		return new JobBuilder(jobName, jobRepository)
			.start(rssPartitionedStep)
			.build();
	}
}
