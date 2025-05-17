package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.embedding.quartz.config;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 수집된 뉴스를 요약하고 임베딩을 계산하는 스케줄 작업
 * application.yml의 spring.quartz.article-embedding.cron 속성으로 실행 주기 설정

 * @since 2025-05-17
 */
@Slf4j
@Configuration
public class ArticleEmbeddingQuartzJobConfig {
	@Getter
	private static final String ARTICLE_EMBEDDING_JOB_DETAIL_NAME = "articleEmbeddingJobDetail";

	/**
	 * RSS 수집 Quartz JobDetail 빈 등록.
	 * Job 클래스는 {@link ArticleEmbeddingJobExecutor}이며 다음과 같은 설정을 포함한다:
	 * - withIdentity("rssBatchJob"): Scheduler 내에서 이 Job을 고유하게 식별하기 위한 이름 지정
	 * - storeDurably(): Trigger가 없더라도 Scheduler에 등록된 상태로 유지되도록 설정
	 *
	 * @return RSS 배치 작업용 JobDetail 객체
	 * @author 함예정
	 * @since 2025-05-17
	 */
	@Bean(ARTICLE_EMBEDDING_JOB_DETAIL_NAME)
	public JobDetail articleEmbeddingJobDetail() {
		return JobBuilder.newJob(ArticleEmbeddingJobExecutor.class)
			.withIdentity(ARTICLE_EMBEDDING_JOB_DETAIL_NAME)
			.storeDurably()
			.build();
	}

}
