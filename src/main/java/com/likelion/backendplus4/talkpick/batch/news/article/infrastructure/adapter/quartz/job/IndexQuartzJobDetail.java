package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.quartz.job;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IndexQuartzJobDetail {
	public static final String indexJobDetailName = "indexJobDetail";

	@Bean(indexJobDetailName)
	public JobDetail indexJobDetail() {
		return JobBuilder.newJob(IndexQuartzJob.class)
			.withIdentity(indexJobDetailName)
			.storeDurably()
			.build();
	}

}
