package com.likelion.backendplus4.talkpick.batch.news.viewcount.infrastructure.quartz.cleanup.config;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis 정리 작업을 위한 Quartz Trigger 설정 클래스입니다.
 *
 * @since 2025-05-29
 */
@Configuration
public class ViewRankingCleanupQuartzTriggerConfig {

    /**
     * Redis 정리 작업을 위한 Trigger를 생성합니다.
     * <p>
     * 매일 새벽 2시에 실행됩니다.
     *
     * @param redisCleanupJobDetail Redis 정리 JobDetail
     * @return Redis 정리 Trigger
     * @author 양병학
     * @since 2025-05-29
     */
    @Bean
    public Trigger redisCleanupTrigger(@Qualifier("redisCleanupJobDetail") JobDetail redisCleanupJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(redisCleanupJobDetail)
                .withIdentity("redisCleanupTrigger", "cleanup")
                .withDescription("매일 새벽 2시 Redis 정리 작업 실행")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 2 * * ?"))
                .build();
    }
}