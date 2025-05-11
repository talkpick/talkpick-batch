package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;


@Component
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class RssQuartzJob implements org.quartz.Job {

	private final JobLauncher jobLauncher;
	private final Job rssJob;
	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		try {
			JobParameters params = new JobParametersBuilder()
				.addLong("timestamp", System.currentTimeMillis())
				.toJobParameters();

			jobLauncher.run(rssJob, params);

		} catch (Exception e) {
			throw new JobExecutionException("RSS Batch 실행 실패", e);
		}
	}
}
