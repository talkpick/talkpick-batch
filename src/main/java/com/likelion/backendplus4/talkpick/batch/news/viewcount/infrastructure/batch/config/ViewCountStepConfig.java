package com.likelion.backendplus4.talkpick.batch.news.viewcount.infrastructure.batch.config;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.ViewCountInvalidFormatException;
import com.likelion.backendplus4.talkpick.batch.news.viewcount.infrastructure.item.ViewCountItem;
import com.likelion.backendplus4.talkpick.batch.news.viewcount.infrastructure.tasklet.OldDataCleanupTasklet;

/**
 * Spring Batch Step 구성 클래스.
 * 뉴스 조회수 동기화를 위한 Step과 관련 컴포넌트를 설정합니다.
 *
 * - viewCountSyncStep: Redis의 조회수 데이터를 DB에 동기화하는 청크 기반 Step
 * - cleanupOldDataStep: 오래된 조회수 데이터를 정리하는 태스크릿 기반 Step
 *
 * @since 2025-05-20
 */
@Configuration
public class ViewCountStepConfig {
    private final String syncStepName = "viewCountSyncStep";
    private final String cleanupStepName = "viewCountCleanupStep";
    private final int chunkSize = 100;
    private final int retryLimit = 3;
    private final int skipLimit = 1000;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ItemReader<ViewCountItem> viewCountReader;
    private final ItemProcessor<ViewCountItem, ViewCountItem> viewCountProcessor;
    private final ItemWriter<ViewCountItem> viewCountWriter;
    private final OldDataCleanupTasklet oldDataCleanupTasklet;

    public ViewCountStepConfig(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<ViewCountItem> viewCountReader,
            ItemProcessor<ViewCountItem, ViewCountItem> viewCountProcessor,
            ItemWriter<ViewCountItem> viewCountWriter,
            OldDataCleanupTasklet oldDataCleanupTasklet) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.viewCountReader = viewCountReader;
        this.viewCountProcessor = viewCountProcessor;
        this.viewCountWriter = viewCountWriter;
        this.oldDataCleanupTasklet = oldDataCleanupTasklet;
    }

    /**
     * 조회수 동기화를 위한 청크 기반 Step을 생성합니다.
     *
     * 1. Redis의 조회수 데이터를 읽고
     * 2. 처리하고
     * 3. DB에 저장
     *
     * 최대 재시도 횟수: 3회
     * 최대 건너뛰기 횟수: 1000건
     *
     * @return 조회수 동기화 Step
     * @since 2025-05-20 최초 작성
     * @author 양병학
     * @modified 2025-05-21 양병학
     *  - ViewCountInvalidFormatException 예외 처리 추가
     */
    @Bean
    public Step viewCountSyncStep() {
        return new StepBuilder(syncStepName, jobRepository)
                .<ViewCountItem, ViewCountItem>chunk(chunkSize, transactionManager)
                .reader(viewCountReader)
                .processor(viewCountProcessor)
                .writer(viewCountWriter)
                .faultTolerant()
                .retry(ArticleCollectorException.class)
                .retryLimit(retryLimit)
                .skip(ArticleCollectorException.class)
                .skipLimit(skipLimit)
                .noSkip(ViewCountInvalidFormatException.class)
                .build();
    }

    /**
     * 오래된 데이터 정리를 위한 태스크릿 기반 Step을 생성합니다.
     * 정리 정책에 따라 Redis의 오래된 데이터를 정리합니다.
     *
     * @return 데이터 정리 Step
     * @since 2025-05-20
     */
    @Bean
    public Step viewCountCleanupStep() {
        return new StepBuilder(cleanupStepName, jobRepository)
                .tasklet(oldDataCleanupTasklet, transactionManager)
                .build();
    }
}