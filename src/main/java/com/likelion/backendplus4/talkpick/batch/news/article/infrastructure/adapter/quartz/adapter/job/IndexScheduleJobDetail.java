package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.quartz.adapter.job;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

@Configuration
public class IndexScheduleJobDetail {
	private final String indexJobDetailName = "indexJobDetail";

	@Bean("indexJobDetail")
	public JobDetail indexJobDetail() {
		return JobBuilder.newJob(IndexScheduleJob.class)
			.withIdentity(indexJobDetailName)
			.storeDurably()
			.build();
	}

}
