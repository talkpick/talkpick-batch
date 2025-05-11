package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.rss;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RSS 뉴스 소스와 URL을 정의하는 열거형
 * 각 항목은 언론사, 카테고리, URL 정보를 포함
 *
 * @author 양병학
 * @since 2025-05-10
 */
@Getter
public enum RssSource {
    // 국민일보 RSS 피드
    KMIB_POLITICS("국민일보", "정치", "https://www.kmib.co.kr/rss/data/kmibPolRss.xml", "km", true),
    KMIB_ECONOMY("국민일보", "경제", "https://www.kmib.co.kr/rss/data/kmibEcoRss.xml", "km", true),
    KMIB_SOCIETY("국민일보", "사회", "https://www.kmib.co.kr/rss/data/kmibSocRss.xml", "km", true),
    KMIB_INTERNATIONAL("국민일보", "국제", "https://www.kmib.co.kr/rss/data/kmibIntRss.xml", "km", true),
    KMIB_ENTERTAINMENT("국민일보", "연예", "https://www.kmib.co.kr/rss/data/kmibEntRss.xml", "km", true),
    KMIB_SPORTS("국민일보", "스포츠", "https://www.kmib.co.kr/rss/data/kmibSpoRss.xml", "km", true),

    // 동아일보 RSS 피드 - 현재 비활성화
    DONGA_TOTAL("동아일보", "전체", "https://rss.donga.com/total.xml", "da", false),

    // 경향신문 RSS 피드 - 현재 비활성화
    KHAN_TOTAL("경향신문", "전체", "https://www.khan.co.kr/rss/rssdata/total_news.xml", "kh", false);

    private final String publisherName;
    private final String categoryName;
    private final String url;
    private final String mapperType;
    private final boolean enabled;

    RssSource(String publisherName, String categoryName, String url, String mapperType, boolean enabled) {
        this.publisherName = publisherName;
        this.categoryName = categoryName;
        this.url = url;
        this.mapperType = mapperType;
        this.enabled = enabled;
    }

    /**
     * 언론사 이름과 카테고리를 결합한 표시 이름 반환
     */
    public String getDisplayName() {
        return publisherName + "-" + categoryName;
    }

    /**
     * 매퍼 타입 키 반환
     */
    public String getMapperType() {
        return mapperType;
    }

    /**
     * 언론사 코드 접두사 반환 (대문자)
     */
    public String getCodePrefix() {
        return mapperType.toUpperCase();
    }

    /**
     * 활성화된 모든 소스 반환
     */
    public static List<RssSource> getEnabledSources() {
        return Arrays.stream(values())
                .filter(RssSource::isEnabled)
                .collect(Collectors.toList());
    }

    /**
     * 특정 언론사의 모든 소스 반환
     */
    public static List<RssSource> getSourcesByPublisher(String publisherName) {
        return Arrays.stream(values())
                .filter(source -> source.getPublisherName().equals(publisherName))
                .collect(Collectors.toList());
    }
}