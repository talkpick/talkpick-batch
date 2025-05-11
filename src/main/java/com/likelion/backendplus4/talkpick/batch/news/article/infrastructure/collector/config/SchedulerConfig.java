package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.quartz.RssQuartzJob;

/**
 * RSS 피드를 정기적으로 수집하는 스케줄 작업
 * application.yml의 rss.scheduler.cron 속성으로 실행 주기 설정
 * 설정이 없을 경우 기본값으로 1분마다 실행
 *
 * 1. 서비스를 거쳐서 피드 수집
 * 2. 처리된 항목 수 로깅
 * 3. 예외 발생 시 오류 로깅하고 다음 스케쥴까지 대기
 *
 * @modified 2025-05-11 Spring Scheduler 에서 Quartz Scheduler 으로 전환
 * @modified 2025-05-10 cron 표현식을 application 으로 분리
 * @since 2025-05-10 최초 작성
 *
 */
@Configuration
public class SchedulerConfig {
	public final String cronExpression;

	/**
	 * 생성자 주입을 통해 Cron 표현식을 설정한다.
	 *
	 * @param cronExpression RSS 배치 실행 주기를 정의하는 Cron 표현식
	 *                       application.yml에서 article-collector.quartz.cron 값을 로드 합니다.
	 * @author 함예정
	 * @since 2025-05-10
	 */
	public SchedulerConfig(@Value("${article-collector.quartz.cron}")
							String cronExpression) {
		this.cronExpression = cronExpression;
	}

	@Bean
	public JobDetail rssBatchJobDetail() {
		return JobBuilder.newJob(RssQuartzJob.class)
			.withIdentity("rssBatchJob")
			.storeDurably()
			.build();
	}

	@Bean
	public Trigger rssBatchTrigger() {
		return TriggerBuilder.newTrigger()
			.forJob(rssBatchJobDetail())
			.withIdentity("rssBatchTrigger")
			.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
			.build();
	}
}
