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

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.quartz.adapter.job.IndexScheduleJobDetail;

import lombok.extern.slf4j.Slf4j;

/**
 * Quartz 스케줄링을 위한 트리거 설정 클래스 <p>
 * 해당 클래스는 IndexScheduleJobDetail에 대한 트리거를 설정하고 등록한다.
 *
 * @since 2025-05-29
 */
@Slf4j
@Configuration
public class IndexScheduleTriggerConfig {
	private final String cronExpression;
	private final JobDetail indexJobDetail;

	public IndexScheduleTriggerConfig(
		@Value("${spring.quartz.article-indexer.cron}")
		String cronExpression,
		@Qualifier(IndexScheduleJobDetail.indexJobDetailName)
		JobDetail indexJobDetail) {
		log.info("Index - Quartz Trigger 등록: cron =  {}", cronExpression);

		this.cronExpression = cronExpression;
		this.indexJobDetail = indexJobDetail;
	}

	/**
	 * Indexing 작업을 위한 Quartz Trigger Bean 등록 <p>
	 *
	 * forJob: 실행할 JobDetail 객체를 지정
	 * withIdentity: 트리거의 고유 식별자 설정 (JobDetail 이름 + "trigger")
	 * withSchedule: 크론 표현식을 기반으로 한 스케줄 설정
	 * @return Trigger 인스턴스
	 * @author 함예정
	 * @since 2025-05-29
	 */
	@Bean
	@DependsOn(IndexScheduleJobDetail.indexJobDetailName)
	public Trigger indexJobTrigger() {
		return TriggerBuilder.newTrigger()
			.forJob(indexJobDetail)
			.withIdentity(IndexScheduleJobDetail.indexJobDetailName + "trigger")
			.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
			.build();

	}
}
