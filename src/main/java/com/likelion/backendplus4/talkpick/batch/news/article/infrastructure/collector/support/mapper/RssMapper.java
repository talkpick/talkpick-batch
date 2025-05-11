package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.rss.RssSource;
import com.rometools.rome.feed.synd.SyndEntry;

/**
 * RSS 항목을 ArticleEntity 엔티티로 변환하는 매퍼 인터페이스
 *
 * @author 양병학
 * @since 2025-05-10 최초 작성
 */
public interface RssMapper {
    /**
     * RSS 피드 항목을 ArticleEntity 엔티티로 변환합니다.
     *
     * @param entry 변환할 SyndEntry 객체(rss2.0 구조 지원)
     * @param source RSS 소스 정보
     * @return 변환된 ArticleEntity 엔티티
     */
    ArticleEntity mapToRssNews(SyndEntry entry, RssSource source);

    /**
     * 매퍼 타입을 반환
     *
     * @return 매퍼 타입 (소문자 코드)
     */
    String getMapperType();
}