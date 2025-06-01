package com.likelion.backendplus4.talkpick.batch.news.viewcount.infrastructure.quartz.cleanup.executor;

import com.likelion.backendplus4.talkpick.batch.news.viewcount.application.service.ViewRankingCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

/**
 * Redis 랭킹 정리 작업을 실행하는 Quartz Job Executor 클래스입니다.
 *
 * 헥사고날 DDD 구조에 따라 Infrastructure Layer의 Adapter 역할을 수행하며,
 * Application Layer의 Service를 호출하여 비즈니스 로직을 처리합니다.
 *
 * @since 2025-05-29 최초 작성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ViewRankingCleanupJobExecutor implements Job {

    private final ViewRankingCleanupService viewRankingCleanupService;

    /**
     * Redis 랭킹 정리 작업을 실행합니다.
     *
     * Infrastructure Layer에서 Quartz 스케줄러의 요청을 받아
     * Application Layer의 Use Case를 실행하는 Adapter 역할을 수행합니다.
     *
     * @param context Job 실행 컨텍스트
     * @throws JobExecutionException Job 실행 중 발생한 예외
     * @author 양병학
     * @since 2025-05-29 최초 작성
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            viewRankingCleanupService.cleanupOldRankingKeys();
        } catch (Exception e) {
            handleJobFailure(context, e);
            throw new JobExecutionException("Redis 랭킹 정리 실패", e);
        }
    }

    /**
     * Job 실행 실패를 처리하고 수동 실행 방법을 안내합니다.
     *
     * @param context Job 실행 컨텍스트
     * @param exception 발생한 예외
     */
    private void handleJobFailure(JobExecutionContext context, Exception exception) {
        String manualEndpoint = context.getJobDetail().getJobDataMap().getString("manualEndpoint");
        String endpoint = (manualEndpoint != null) ? manualEndpoint : "/api/admin/news/ranking/cleanup";
        log.error("Redis 랭킹 정리 작업이 실패했습니다. 수동 실행이 필요합니다: POST {}", endpoint, exception);
    }
}