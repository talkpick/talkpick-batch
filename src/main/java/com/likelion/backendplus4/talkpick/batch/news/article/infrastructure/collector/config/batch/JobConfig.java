package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * Spring Batch Job 설정 클래스. <p>
 * RSS 기사 수집을 위한 Batch Job 을 정의하며, 파티셔닝된 Step 을 시작 단계로 구성한다. <p>
 *
 * 이 잡은 {@code articleCollectorBatchJob}이라는 이름으로 정의되며,
 * {@link Step} 객체는 외부에서 주입받아 사용한다. <p>
 *
 * 해당 Job 은 Quartz 또는 Spring Scheduler 를 통해 주기적으로 실행될 수 있다. <p>
 *
 * @since 2025-05-10
 */
@Configuration
public class JobConfig {
	private final String jobName = "articleCollectorBatchJob";
	private final JobRepository jobRepository;
	private final Step rssPartitionedStep;

	public JobConfig(JobRepository jobRepository, Step articleRssPartitionedStep) {
		this.jobRepository = jobRepository;
		this.rssPartitionedStep = articleRssPartitionedStep;
	}

	/**
	 * RSS 기사 수집용 Spring Batch Job Bean을 생성한다.
	 * 파티셔닝 Step 을 실행하도록 구성한다.
	 *
	 * @return RSS 기사 수집 배치 Job
	 * @author 함예정
	 * @since 2025-05-10
	 */
	@Bean
	public Job articleCollectJob() {
		return new JobBuilder(jobName, jobRepository)
			.start(rssPartitionedStep)
			.build();
	}
}
