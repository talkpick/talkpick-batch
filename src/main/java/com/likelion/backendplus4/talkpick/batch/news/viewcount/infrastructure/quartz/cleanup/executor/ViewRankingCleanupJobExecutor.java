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
 * Redis 랭킹 정리 작업을 실행하는 Quartz Job Executor 클래스입니다.
 *
 * 헥사고날 DDD 구조에 따라 Infrastructure Layer의 Adapter 역할을 수행하며,
 * Application Layer의 Service를 호출하여 비즈니스 로직을 처리합니다.
 *
 * @author 양병학
 * @since 2025-05-29 최초 작성
 * @modify 2025-06-01 양병학
 *  - 헥사고날 DDD 패턴에 맞게 private 메서드로 세분화
 *  - 로그 메시지 한국어로 변경
 *  - 책임 분리를 통한 가독성 향상
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
        executeJobWithErrorHandling();
    }

    /**
     * 에러 처리와 함께 전체 작업을 실행합니다.
     *
     * @author 양병학
     * @since 2025-06-01 최초 작성
     */
    private void executeJobWithErrorHandling() {
        LocalDateTime startTime = recordJobStartTime();

        try {
            executeCleanupProcess();
            recordJobSuccessCompletion(startTime);
        } catch (Exception e) {
            handleJobExecutionFailure(startTime, e);
        }
    }

    /**
     * 작업 시작 시간을 기록하고 로그를 출력합니다.
     *
     * @return 작업 시작 시간
     * @author 양병학
     * @since 2025-06-01 최초 작성
     */
    private LocalDateTime recordJobStartTime() {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("Redis 랭킹 정리 배치 작업을 시작합니다. 시작 시간: {}", startTime);
        return startTime;
    }

    /**
     * 실제 랭킹 정리 프로세스를 실행합니다.
     *
     * Application Layer의 Use Case를 호출하여 비즈니스 로직을 처리합니다.
     * Infrastructure Layer는 단순히 요청을 전달하는 역할만 수행합니다.
     *
     * @author 양병학
     * @since 2025-06-01 최초 작성
     */
    private void executeCleanupProcess() {
        log.debug("ViewRankingCleanupService를 통해 랭킹 정리 작업을 실행합니다.");
        viewRankingCleanupService.cleanupOldRankingKeys();
    }

    /**
     * 작업 성공 완료를 기록하고 로그를 출력합니다.
     *
     * @param startTime 작업 시작 시간
     * @author 양병학
     * @since 2025-06-01 최초 작성
     */
    private void recordJobSuccessCompletion(LocalDateTime startTime) {
        LocalDateTime endTime = LocalDateTime.now();
        log.info("Redis 랭킹 정리 배치 작업이 성공적으로 완료되었습니다. " +
                "시작 시간: {}, 종료 시간: {}", startTime, endTime);
    }

    /**
     * 작업 실행 실패를 처리하고 로그를 출력합니다.
     *
     * Quartz의 자동 재시도를 방지하기 위해 JobExecutionException을 발생시키지 않습니다.
     * 대신 수동 실행을 안내하는 로그를 출력합니다.
     *
     * @param startTime 작업 시작 시간
     * @param exception 발생한 예외
     * @author 양병학
     * @since 2025-06-01 최초 작성
     */
    private void handleJobExecutionFailure(LocalDateTime startTime, Exception exception) {
        LocalDateTime failureTime = LocalDateTime.now();
        log.error("Redis 랭킹 정리 배치 작업이 실패했습니다. " +
                        "시작 시간: {}, 실패 시간: {}. " +
                        "관리자 API 엔드포인트를 통한 수동 실행이 필요합니다.",
                startTime, failureTime, exception);

        log.error("수동 실행 방법: POST /api/admin/news/ranking/cleanup");
    }
}