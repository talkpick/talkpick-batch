package com.likelion.backendplus4.talkpick.batch.news.viewcount.infrastructure.quartz.cleanup.config;

import com.likelion.backendplus4.talkpick.batch.news.viewcount.infrastructure.quartz.cleanup.job.ViewRankingCleanupQuartzJob;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis 정리 작업을 위한 Quartz Job 설정 클래스입니다.
 *
 * @since 2025-05-29
 */
@Configuration
public class ViewRankingCleanupQuartzJobConfig {

    /**
     * Redis 정리 작업을 위한 JobDetail을 생성합니다.
     *
     * @return Redis 정리 JobDetail
     * @author 양병학
     * @since 2025-05-29
     * @modified 2025-06-01 양병학
     */
    @Bean
    public JobDetail redisCleanupJobDetail() {
        return JobBuilder.newJob(ViewRankingCleanupQuartzJob.class)
                .withIdentity("redisCleanupJob", "cleanup")
                .withDescription("Redis 오래된 랭킹 데이터 정리 작업")
                .usingJobData("manualEndpoint", "/api/admin/news/ranking/cleanup")
                .usingJobData("jobDescription", "Redis 랭킹 데이터 정리")
                .storeDurably()
                .build();
    }
}