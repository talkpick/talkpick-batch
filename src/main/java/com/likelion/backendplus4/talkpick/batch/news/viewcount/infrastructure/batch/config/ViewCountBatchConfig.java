package com.likelion.backendplus4.talkpick.batch.news.viewcount.infrastructure.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Batch Job 설정 클래스.
 * 뉴스 조회수 데이터를 Redis에서 DB로 동기화하는 Batch Job을 정의합니다.
 *
 * @since 2025-05-20
 * @author 양병학
 */
@Configuration
public class ViewCountBatchConfig {
    private final String jobName = "viewCountSyncJob";
    private final JobRepository jobRepository;
    private final Step viewCountSyncStep;
    private final Step viewCountCleanupStep;

    public ViewCountBatchConfig(JobRepository jobRepository,
                                Step viewCountSyncStep,
                                Step viewCountCleanupStep) {
        this.jobRepository = jobRepository;
        this.viewCountSyncStep = viewCountSyncStep;
        this.viewCountCleanupStep = viewCountCleanupStep;
    }

    /**
     * 뉴스 조회수 동기화용 Spring Batch Job Bean을 생성합니다.
     *
     * 1. 조회수 동기화 Step 실행
     * 2. 오래된 데이터 정리 Step 실행
     *
     * @return 뉴스 조회수 동기화 배치 Job
     * @since 2025-05-20 최초 작성
     * @author 양병학
     *
     */
    @Bean
    public Job viewCountSyncJob() {
        return new JobBuilder(jobName, jobRepository)
                .start(viewCountSyncStep)
                .next(viewCountCleanupStep)
                .build();
    }
}