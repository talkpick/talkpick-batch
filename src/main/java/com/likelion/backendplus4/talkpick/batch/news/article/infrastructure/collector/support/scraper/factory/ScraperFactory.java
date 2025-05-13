package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.ContentScraper;

/**
 * 뉴스 스크래퍼 사용하는 factory Class
 *
 * @author 양병학
 * @since 2025-05-13 최초 작성
 */
@Component
public class ScraperFactory {

    private final Map<String, ContentScraper> scrapers = new HashMap<>();

    /**
     * ContentScraper 구현체 등록
     *
     * @param availableScrapers ContentScraper 목록
     */
    @Autowired
    public ScraperFactory(List<ContentScraper> availableScrapers) {
        for (ContentScraper scraper : availableScrapers) {
            String mapperType = scraper.getSupportedMapperType();
            scrapers.put(mapperType, scraper);
        }
    }

    /**
     * Mapper Type에 맞는 스크래퍼 반환
     *
     * @param mapperType 매퍼 타입 (예: "km", "da")
     * @return 해당 타입의 스크래퍼 or null일시 Optional로 빈 값 반환
     */
    public Optional<ContentScraper> getScraper(String mapperType) {
        return Optional.ofNullable(scrapers.get(mapperType));
    }
}