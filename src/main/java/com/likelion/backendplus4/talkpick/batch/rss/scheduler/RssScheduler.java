package com.likelion.backendplus4.talkpick.batch.rss.scheduler;

import com.likelion.backendplus4.talkpick.batch.rss.service.RssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class RssScheduler {

    private final RssService rssService;

    @Scheduled(cron = "0 */1 * * * ?")
    public void scheduleRssFeedFetch() {
        log.info("Quartz 정상시작");

        try {
            int processedItems = rssService.fetchAndProcessAllFeeds();
            log.info("Rss 피드 입력 Processed {} items", processedItems);
        } catch (Exception e) {
            log.error("Error during scheduled RSS feed fetch: {}", e.getMessage(), e);
        }
    }
}