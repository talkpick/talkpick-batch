package com.likelion.backendplus4.talkpick.batch.news.viewcount.infrastructure.quartz.executor;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * Quartz에 의해 트리거되는 Spring Batch Job 실행 클래스.
 * JobLauncher를 통해 뉴스 조회수 동기화 Job을 수동으로 실행하며,
 * 매 실행 시 고유한 JobParameters를 생성하여 중복 실행을 방지합니다.
 *
 * @since 2025-05-20
 */
@Component
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class ViewCountSyncJobExecutor implements org.quartz.Job {
    private final JobLauncher jobLauncher;
    private final Job viewCountSyncJob;

    /**
     * Quartz 트리거에 의해 호출되는 메서드.
     * 내부적으로 Spring Batch Job을 실행하는 로직을 위임합니다.
     *
     * @param jobExecutionContext Quartz 실행 컨텍스트
     * @since 2025-05-20
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        startSpringBatchJob();
    }

    /**
     * Spring Batch Job을 JobLauncher를 통해 실행합니다.
     *
     * 1. 고유한 timestamp 파라미터 생성
     * 2. jobLauncher를 통해 Job 실행
     * 3. 예외 발생 시 도메인 예외로 변환
     *
     * @since 2025-05-20 최초 작성
     * @author 양병학
     *
     * // FIXME: 실패 시 알림 로직 추가 필요
     */
    private void startSpringBatchJob() {
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        try {
            jobLauncher.run(viewCountSyncJob, params);
        } catch (Exception e) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.VIEW_COUNT_SYNC_FAILED, e);
        }
    }
}