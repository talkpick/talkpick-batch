package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.quartz.trigger;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class IndexScheduleTriggerConfig {
	private final String cronExpression;
	private final JobDetail indexJobDetail;
	private final String indexJobDetailName = "indexJobDetail";

	public IndexScheduleTriggerConfig(
		@Value("${spring.quartz.article-indexer.cron}")
		String cronExpression,
		@Qualifier("indexJobDetail")
		JobDetail indexJobDetail) {
		log.info("Index - Quartz Trigger 등록: cron =  {}", cronExpression);

		this.cronExpression = cronExpression;
		this.indexJobDetail = indexJobDetail;
	}

	@Bean
	@DependsOn("indexJobDetail")
	public Trigger indexJobTrigger() {
		return TriggerBuilder.newTrigger()
			.forJob(indexJobDetail)
			.withIdentity(indexJobDetailName + "trigger")
			.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
			.build();

	}
}
