package com.likelion.backendplus4.talkpick.batch.news.rss.infrastructure.scheduler;

import com.likelion.backendplus4.talkpick.batch.news.rss.infrastructure.exception.RssErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.rss.infrastructure.exception.RssException;
import com.likelion.backendplus4.talkpick.batch.news.rss.infrastructure.service.RssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * RSS 피드 수집 작업을 스케줄링하는 클래스
 *
 * @author 양병학
 * @since 2025-05-10 최초 작성
 * @modify 2025-05-10 cron 표현식을 설정 파일로 분리하여 유연성 개선
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class RssScheduler {

    private final RssService rssService;

    /**
     * RSS 피드를 정기적으로 수집하는 스케줄 작업
     * application.yml의 rss.scheduler.cron 속성으로 실행 주기 설정
     * 설정이 없을 경우 기본값으로 1분마다 실행
     *
     * 1. 서비스를 거쳐서 피드 수집
     * 2. 처리된 항목 수 로깅
     * 3. 예외 발생 시 오류 로깅하고 다음 스케쥴까지 대기
     *
     * @since 2025-05-10 최초 작성
     * @modify 2025-05-10 cron 표현식을 application으로 분리
     */
    @Scheduled(cron = "${rss.scheduler.cron:0 */1 * * * ?}")
    public void scheduleRssFeedFetch() {
        log.info("Quartz 정상시작");

        try {
            int processedItems = rssService.fetchAndProcessAllFeeds();
            log.info("Rss 피드 입력 Processed {} items", processedItems);
        } catch (RssException e) {
            // 커스텀 예외 처리
            log.error("[{}] 스케줄러 실행 오류: {}", e.getErrorCode().getCode(), e.getMessage(), e);
        } catch (Exception e) {
            // 기타 예외 처리
            log.error("[{}] 스케줄러 실행 중 예상치 못한 오류: {}",
                    RssErrorCode.SCHEDULER_EXECUTION_ERROR.getCode(), e.getMessage(), e);
        }
    }
}