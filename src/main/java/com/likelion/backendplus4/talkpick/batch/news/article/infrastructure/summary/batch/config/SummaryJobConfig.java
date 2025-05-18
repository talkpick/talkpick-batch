package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.summary.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Batch Job 설정 클래스. <p>
 * 수집된 기사를 OpenAI를 활용해 뉴스 내용을 요약하는
 * Batch Job 을 정의하며, 파티셔닝된 Step 을 시작 단계로 구성한다. <p>
 *
 * 이 잡은 {@code articleCollectorBatchJob}이라는 이름으로 정의되며,
 * {@link Step} 객체는 외부에서 주입받아 사용한다. <p>
 *
 * 해당 Job 은 Quartz 또는 Spring Scheduler 를 통해 주기적으로 실행될 수 있다. <p>
 *
 * @since 2025-05-17
 */
@Configuration
public class SummaryJobConfig {
	private final String jobName = "summaryJob";
	private final JobRepository jobRepository;
	private final Step articleSummaryStep;

	public SummaryJobConfig(JobRepository jobRepository, Step articleSummaryStep) {
		this.jobRepository = jobRepository;
		this.articleSummaryStep = articleSummaryStep;
	}

	/**
	 * 뉴스 내용을 요약하는 Spring Batch Job Bean을 생성한다.
	 * 파티셔닝 Step 을 실행하도록 구성한다.
	 *
	 * @return 뉴스 요약 Batch Job
	 * @author 함예정
	 * @since 2025-05-10
	 */
	@Bean
	public Job summaryJob() {
		return new JobBuilder(jobName, jobRepository)
			.start(articleSummaryStep)
			.build();
	}
}
