package com.likelion.backendplus4.talkpick.batch.news.viewcount.infrastructure.quartz.config;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 조회수 동기화를 위한 Quartz Trigger 설정 클래스.
 * Cron 표현식을 사용하여 매일 자정에 동기화 작업이 실행되도록 설정합니다.
 *
 * @since 2025-05-25
 * @author 양병학
 */
@Configuration
public class ViewCountQuartzTriggerConfig {
	private final String cronExpression;
	private final JobDetail viewCountSyncJobDetail;
	private final String viewCountSyncJobDetailName =
		ViewCountQuartzJobConfig.getVIEW_COUNT_SYNC_JOB_DETAIL_NAME();

	/**
	 * 생성자 주입을 통해 Cron 표현식을 설정합니다.
	 *
	 * @param cronExpression 조회수 동기화 배치 실행 주기를 정의하는 Cron 표현식
	 *                       application.yml에서 spring.quartz.view-count-sync.cron 값을 로드합니다.
	 * @param viewCountSyncJobDetail 조회수 동기화 JobDetail
	 * @since 2025-05-25
	 */
	public ViewCountQuartzTriggerConfig(
		@Value("${spring.quartz.view-count-sync.cron}") String cronExpression,
		@Qualifier("viewCountSyncJobDetail")
		JobDetail viewCountSyncJobDetail) {
		this.cronExpression = cronExpression;
		this.viewCountSyncJobDetail = viewCountSyncJobDetail;
	}

	/**
	 * 조회수 동기화 Quartz Trigger 빈 등록.
	 * - forJob: 이 Trigger가 어떤 Quartz Job과 연관되어 실행될지를 지정
	 * - withIdentity: Scheduler 내에서 이 Trigger를 고유하게 식별하기 위한 이름 지정
	 * - withSchedule: Cron 표현식을 사용하여 실행 주기 설정 (기본값: 매일 자정)
	 *
	 * @return 조회수 동기화 작업용 Trigger 객체
	 * @since 2025-05-25
	 */
	@Bean
	public Trigger viewCountSyncTrigger() {
		return TriggerBuilder.newTrigger()
			.forJob(viewCountSyncJobDetail)
			.withIdentity(viewCountSyncJobDetailName + "Trigger")
			.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
			.build();
	}
}