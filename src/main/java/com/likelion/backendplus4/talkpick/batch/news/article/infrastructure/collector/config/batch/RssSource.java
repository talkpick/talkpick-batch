package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.batch;

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
 * @modified 2025-05-12 표준 카테고리(NewsCategory) 도입 및 동아일보, 경향신문 카테고리별 피드 추가
 */
@Getter
public enum RssSource {
    // 국민일보 RSS 피드
    KMIB_POLITICS("국민일보", NewsCategory.POLITICS, "https://www.kmib.co.kr/rss/data/kmibPolRss.xml", "km", true),
    KMIB_ECONOMY("국민일보", NewsCategory.ECONOMY, "https://www.kmib.co.kr/rss/data/kmibEcoRss.xml", "km", true),
    KMIB_SOCIETY("국민일보", NewsCategory.SOCIETY, "https://www.kmib.co.kr/rss/data/kmibSocRss.xml", "km", true),
    KMIB_INTERNATIONAL("국민일보", NewsCategory.INTERNATIONAL, "https://www.kmib.co.kr/rss/data/kmibIntRss.xml", "km", true),
    KMIB_ENTERTAINMENT("국민일보", NewsCategory.ENTERTAINMENT, "https://www.kmib.co.kr/rss/data/kmibEntRss.xml", "km", true),
    KMIB_SPORTS("국민일보", NewsCategory.SPORTS, "https://www.kmib.co.kr/rss/data/kmibSpoRss.xml", "km", true),

    // 동아일보 RSS 피드
    DONGA_POLITICS("동아일보", NewsCategory.POLITICS, "https://rss.donga.com/politics.xml", "da", true),
    DONGA_ECONOMY("동아일보", NewsCategory.ECONOMY, "https://rss.donga.com/economy.xml", "da", true),
    DONGA_SOCIETY("동아일보", NewsCategory.SOCIETY, "https://rss.donga.com/national.xml", "da", true),
    DONGA_INTERNATIONAL("동아일보", NewsCategory.INTERNATIONAL, "https://rss.donga.com/international.xml", "da", true),
    DONGA_ENTERTAINMENT("동아일보", NewsCategory.ENTERTAINMENT, "https://rss.donga.com/entertainment.xml", "da", true),
    DONGA_SPORTS("동아일보", NewsCategory.SPORTS, "https://rss.donga.com/sports.xml", "da", true),

    // 경향신문 RSS 피드
    KHAN_POLITICS("경향신문", NewsCategory.POLITICS, "https://www.khan.co.kr/rss/rssdata/politic_news.xml", "kh", true),
    KHAN_ECONOMY("경향신문", NewsCategory.ECONOMY, "https://www.khan.co.kr/rss/rssdata/economy_news.xml", "kh", true),
    KHAN_SOCIETY("경향신문", NewsCategory.SOCIETY, "https://www.khan.co.kr/rss/rssdata/society_news.xml", "kh", true),
    KHAN_INTERNATIONAL("경향신문", NewsCategory.INTERNATIONAL, "https://www.khan.co.kr/rss/rssdata/world_news.xml", "kh", true),
    KHAN_ENTERTAINMENT("경향신문", NewsCategory.ENTERTAINMENT, "https://www.khan.co.kr/rss/rssdata/art_news.xml", "kh", true),
    KHAN_SPORTS("경향신문", NewsCategory.SPORTS, "https://www.khan.co.kr/rss/rssdata/sports_news.xml", "kh", true);

    private final String publisherName;
    private final NewsCategory category;
    private final String url;
    private final String mapperType;
    private final boolean enabled;

    RssSource(String publisherName, NewsCategory category, String url, String mapperType, boolean enabled) {
        this.publisherName = publisherName;
        this.category = category;
        this.url = url;
        this.mapperType = mapperType;
        this.enabled = enabled;
    }

    /**
     * 카테고리 이름 반환
     *
     * @return 카테고리 표시 이름
     */
    public String getCategoryName() {
        return category.getDisplayName();
    }

    /**
     * 언론사 이름과 카테고리를 결합한 표시 이름 반환
     */
    public String getDisplayName() {
        return publisherName + "-" + getCategoryName();
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

    /**
     * 특정 카테고리의 모든 소스 반환
     *
     * @param category 검색할 카테고리
     * @return 해당 카테고리의 활성화된 소스 목록
     */
    public static List<RssSource> getSourcesByCategory(NewsCategory category) {
        return Arrays.stream(values())
                .filter(RssSource::isEnabled)
                .filter(source -> source.getCategory() == category)
                .collect(Collectors.toList());
    }
}