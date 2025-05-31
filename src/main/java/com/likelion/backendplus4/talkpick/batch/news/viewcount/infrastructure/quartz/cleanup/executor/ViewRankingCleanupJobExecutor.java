package com.likelion.backendplus4.talkpick.batch.news.viewcount.infrastructure.quartz.cleanup.executor;

import com.likelion.backendplus4.talkpick.batch.news.viewcount.application.service.ViewRankingCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Redis 정리 작업을 실행하는 Quartz Job Executor 클래스입니다.
 *
 * @since 2025-05-29 최초 작성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ViewRankingCleanupJobExecutor implements Job {

    private final ViewRankingCleanupService redisCleanupService;

    /**
     * Redis 정리 작업을 실행합니다.
     *
     * 1. 실행 시작 로그 기록
     * 2. Redis 정리 서비스 호출
     * 3. 성공/실패 로그 기록
     * 4. 실패 시에도 JobExecutionException 발생시키지 않음 (Quartz 재시도 방지)
     *
     * @param context Job 실행 컨텍스트
     * @throws JobExecutionException Job 실행 중 발생한 예외
     * @author 양병학
     * @since 2025-05-29 최초 작성
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("Redis cleanup job started at {}", startTime);

        try {
            redisCleanupService.cleanupOldRankingKeys();
            log.info("Redis cleanup job completed successfully at {}", LocalDateTime.now());
        } catch (Exception e) {
            log.error("Redis cleanup job failed at {}. Manual execution required via controller endpoint.",
                    LocalDateTime.now(), e);
        }
    }
}