package com.likelion.backendplus4.talkpick.batch.news.viewcount.infrastructure.batch.config;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import com.likelion.backendplus4.talkpick.batch.news.viewcount.infrastructure.item.ViewCountDatabaseWriter;
import com.likelion.backendplus4.talkpick.batch.news.viewcount.infrastructure.item.ViewCountItem;
import com.likelion.backendplus4.talkpick.batch.news.viewcount.infrastructure.item.ViewCountProcessor;
import com.likelion.backendplus4.talkpick.batch.news.viewcount.infrastructure.item.ViewCountRedisReader;
import com.likelion.backendplus4.talkpick.batch.news.viewcount.infrastructure.manager.ViewCountBatchManager;
import com.likelion.backendplus4.talkpick.batch.news.viewcount.infrastructure.tasklet.OldDataCleanupTasklet;

/**
 * Spring Batch Item 구성 클래스.
 * 조회수 동기화를 위한 Reader, Processor, Writer 및 Tasklet을 설정합니다.
 *
 * @since 2025-05-20
 * @author 양병학
 * @modified 2025-05-23 ViewCountUpdateService 추가
 */
@Configuration
public class ViewCountItemConfig {

    private final RedisTemplate<String, String> redisTemplate;
    private final ViewCountBatchManager viewCountBatchManager;
    private final NewsViewCountProperties newsViewCountProperties;

    public ViewCountItemConfig(
        RedisTemplate<String, String> redisTemplate,
        ViewCountBatchManager viewCountBatchManager,
        NewsViewCountProperties newsViewCountProperties) {
        this.redisTemplate = redisTemplate;
        this.viewCountBatchManager = viewCountBatchManager;
        this.newsViewCountProperties = newsViewCountProperties;
    }

    /**
     * Redis에서 조회수 데이터를 읽는 Reader를 생성합니다.
     * "news:viewCount:*" 패턴에 일치하는 모든 키를 조회합니다.
     *
     * @return 조회수 데이터 Reader
     * @since 2025-05-20 최초 작성
     * @author 양병학
     */
    @Bean
    public ItemReader<ViewCountItem> viewCountReader() {
        return new ViewCountRedisReader(redisTemplate, "news:viewCount:*", newsViewCountProperties);
    }

    /**
     * 조회수 데이터를 처리하는 Processor를 생성합니다.
     *
     * @return 조회수 데이터 Processor
     * @since 2025-05-20
     */
    @Bean
    public ItemProcessor<ViewCountItem, ViewCountItem> viewCountProcessor() {
        return new ViewCountProcessor();
    }

    /**
     * 처리된 조회수 데이터를 DB에 저장하는 Writer를 생성합니다.
     *
     * @return 조회수 데이터 Writer
     * @since 2025-05-20
     */
    @Bean
    public ItemWriter<ViewCountItem> viewCountWriter() {
        return new ViewCountDatabaseWriter(viewCountBatchManager);
    }

    /**
     * 오래된 조회수 데이터를 정리하는 Tasklet을 생성합니다.
     *
     * @return 데이터 정리 Tasklet
     * @since 2025-05-20
     */
    @Bean
    public OldDataCleanupTasklet oldDataCleanupTasklet() {
        return new OldDataCleanupTasklet(redisTemplate);
    }
}