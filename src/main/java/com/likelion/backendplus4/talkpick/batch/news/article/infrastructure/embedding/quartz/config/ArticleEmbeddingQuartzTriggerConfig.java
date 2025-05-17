package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.embedding.quartz.config;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ArticleEmbeddingQuartzTriggerConfig {
	private final String cronExpression;
	private final JobDetail articleEmbeddingJobDetail;
	private final String articleEmbeddingJobDetailName =
		ArticleEmbeddingQuartzJobConfig.getARTICLE_EMBEDDING_JOB_DETAIL_NAME();

	/**
	 * 생성자 주입을 통해 Cron 표현식을 설정한다.
	 *
	 * @param cronExpression RSS 배치 실행 주기를 정의하는 Cron 표현식
	 *                       application.yml에서 spring.quartz.article-embedding.cron 값을 로드 합니다.
	 * @author 함예정
	 * @since 2025-05-17
	 */
	public ArticleEmbeddingQuartzTriggerConfig(
		@Value("${spring.quartz.article-embedding.cron}") String cronExpression,
		@Qualifier("articleEmbeddingJobDetail")
		JobDetail articleEmbeddingJobDetail) {
		this.cronExpression = cronExpression;
		this.articleEmbeddingJobDetail = articleEmbeddingJobDetail;
	}

	/**
	 * 내용 임베딩 작업을 위한 Quartz Trigger 빈 등록.
	 * - forJob: 이 Trigger 가 어떤 Quartz Job 과 연관되어 실행될지를 지정
	 * - withIdentity: Scheduler 내에서 이 Trigger 를 고유하게 식별하기 위한 이름 지정
	 * - withSchedule: Cron 표현식을 사용하여 실행 주기 설정
	 *
	 * @return RSS 배치 작업용 Trigger 객체
	 * @author 함예정
	 * @since 2025-05-17
	 */
	@Bean
	public Trigger articleEmbeddingQuartzTrigger() {
		log.info("Quartz Trigger: " + articleEmbeddingJobDetailName);
		return TriggerBuilder.newTrigger()
			.forJob(articleEmbeddingJobDetail)
			.withIdentity(articleEmbeddingJobDetailName + "trigger")
			.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
			.build();
	}
}
