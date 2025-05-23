package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.batch;

/**
 * 뉴스 기사 표준 카테고리
 *
 * @author 양병학
 * @since 2025-05-12
 */
public enum NewsCategory {
    POLITICS("정치"),
    ECONOMY("경제"),
    SOCIETY("사회"),
    INTERNATIONAL("국제"),
    ENTERTAINMENT("연예"),
    SPORTS("스포츠"),
    TOTAL("전체");

    private final String displayName;

    NewsCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}