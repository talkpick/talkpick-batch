package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.result;

public class ScrapingResult {
    private final String description;
    private final String imageUrl;

    public ScrapingResult(String description, String imageUrl) {
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}