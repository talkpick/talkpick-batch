package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.quartz;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzTriggerConfig {
	private final String cronExpression;
	private final JobDetail articleCollectorBatchJobDetail;
	private final String articleCollectorBatchJobDetailName = "rssBatchJob";

	/**
	 * 생성자 주입을 통해 Cron 표현식을 설정한다.
	 *
	 * @param cronExpression RSS 배치 실행 주기를 정의하는 Cron 표현식
	 *                       application.yml에서 article-collector.quartz.cron 값을 로드 합니다.
	 * @author 함예정
	 * @since 2025-05-10
	 */
	public QuartzTriggerConfig(@Value("${article-collector.quartz.cron}") String cronExpression,
								JobDetail articleCollectorBatchJobDetail) {
		this.cronExpression = cronExpression;
		this.articleCollectorBatchJobDetail = articleCollectorBatchJobDetail;
	}

	/**
	 * RSS 수집 Quartz Trigger 빈 등록.
	 *  - forJob: 이 Trigger 가 어떤 Quartz Job 과 연관되어 실행될지를 지정
	 * - withIdentity: Scheduler 내에서 이 Trigger 를 고유하게 식별하기 위한 이름 지정
	 * - withSchedule: Cron 표현식을 사용하여 실행 주기 설정
	 *
	 * @return RSS 배치 작업용 Trigger 객체
	 * @author 함예정
	 * @since 2025-05-10
	 */
	@Bean
	public Trigger rssBatchTrigger() {
		return TriggerBuilder.newTrigger()
			.forJob(articleCollectorBatchJobDetail)
			.withIdentity(articleCollectorBatchJobDetailName)
			.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
			.build();
	}
}
