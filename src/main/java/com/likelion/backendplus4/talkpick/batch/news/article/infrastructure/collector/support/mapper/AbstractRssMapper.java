package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.batch.RssSource;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.result.ScrapingResult;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.ContentScraper;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.factory.ScraperFactory;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.util.HtmlParser;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.util.ParagraphUtil;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RSS를 ArticleEntity로 변환하는 추상 클래스
 * 공통 변환 로직을 제공
 *
 * @author 양병학
 * @since 2025-05-13 최초 작성
 * @modified 2025-05-15 의존성 주입 방식 개선 (템플릿 메서드 패턴 적용)
 */

public abstract class AbstractRssMapper {

    protected abstract ScraperFactory getScraperFactory();

    /**
     * 매퍼의 유형을 식별하는 코드 반환
     * 소문자 언론사 코드 형태 (예: "km", "da", "kh")
     */
    public abstract String getMapperType();

    /**
     * RSS 피드를 ArticleEntity 엔티티로 변환하는 템플릿 메소드
     *
     * @param entry 변환할 SyndEntry(Rss 데이터) 객체
     * @param source RSS 소스 정보
     * @return 변환된 ArticleEntity 엔티티
     */
    public final ArticleEntity mapToRssNews(SyndEntry entry, RssSource source) {
        String guid = extractGuid(entry, source);
        String title = extractTitle(entry);
        String link = extractLink(entry);
        LocalDateTime pubDate = extractPubDate(entry);
        String category = extractCategory(entry, source);
        String imageUrl = extractImageUrl(entry);

        String baseDescription = extractDescription(entry);
        ScrapingResult result = performSpecificMapping(entry, source, link, baseDescription, imageUrl);

        return ArticleEntity.builder()
                .title(title)
                .link(link)
                .pubDate(pubDate)
                .category(category)
                .guid(guid)
                .description(result.getDescription())
                .imageUrl(result.getImageUrl())
                .build();
    }

    /**
     * 매퍼 유형에 따른 처리를 수행하는 추상 메소드
     *
     * @param entry RSS 항목
     * @param source RSS 소스 정보
     * @param link 기사 링크
     * @param baseDescription RSS에서 추출한 기본 설명
     * @param baseImageUrl RSS에서 추출한 기본 이미지 URL
     * @return 매핑 결과 (설명과 이미지 URL)
     */
    protected abstract ScrapingResult performSpecificMapping(
            SyndEntry entry,
            RssSource source,
            String link,
            String baseDescription,
            String baseImageUrl);

    /**
     * Date 객체를 LocalDateTime으로 변환
     *
     * @param date 변환할 Date 객체
     * @return 변환된 LocalDateTime 객체, date가 null이면 현재 시간 반환
     */
    protected LocalDateTime convertToLocalDateTime(Date date) {
        return (null != date)
                ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                : LocalDateTime.now();
    }

    /**
     * 제목 추출 메서드
     */
    protected String extractTitle(SyndEntry entry) {
        return entry.getTitle();
    }

    /**
     * 링크 추출 메서드
     *
     * @param entry RSS 항목
     * @return 링크
     */
    protected String extractLink(SyndEntry entry) {
        return entry.getLink();
    }

    /**
     * 발행일 추출 메서드
     *
     * @param entry RSS 항목
     * @return 발행일
     */
    protected LocalDateTime extractPubDate(SyndEntry entry) {
        return convertToLocalDateTime(entry.getPublishedDate());
    }

    /**
     * 설명 추출 메서드
     *
     * @param entry RSS 항목
     * @return 추출된 설명
     */
    protected String extractDescription(SyndEntry entry) {
        return  null != entry.getDescription() ? entry.getDescription().getValue() : null;
    }

    /**
     * 이미지 URL 추출 메서드
     * media:content 태그에서 이미지 URL 추출
     *
     * @param entry RSS 항목
     * @return 이미지 URL
     */
    protected String extractImageUrl(SyndEntry entry) {
        return entry.getForeignMarkup().stream()
                .filter(element -> "content".equals(element.getName()) &&
                        "media".equals(element.getNamespacePrefix()))
                .findFirst()
                .map(element -> element.getAttributeValue("url"))
                .orElse("");
    }

    /**
     * 카테고리 추출 메서드
     *
     * @param entry RSS 항목
     * @param source RSS 소스 정보
     * @return 카테고리
     */
    protected String extractCategory(SyndEntry entry, RssSource source) {
        return source.getCategoryName();
    }

    /**
     * GUID 추출 메서드 - 하위 클래스에서 구현해야 함
     *
     * @param entry RSS 항목
     * @param source RSS 소스 정보
     * @return GUID
     */
    /**
     * GUID 추출을 위한 템플릿 메소드
     *
     * @param entry RSS 항목
     * @param source RSS 소스 정보
     * @return 신문사 코드 + 고유 ID 형태의 GUID
     * @throws ArticleCollectorException 링크가 없거나 ID 추출 실패 시
     */
    protected final String extractGuid(SyndEntry entry, RssSource source) {
        validateEntryLink(entry.getLink());

        String uniqueId = extractUniqueIdFromLink(entry.getLink());
        validateUniqueId(uniqueId);

        return source.getCodePrefix() + uniqueId;
    }

    /**
     * 링크에서 고유 ID를 추출하는 추상 메소드
     * 각 매퍼가 자신의 URL 패턴에 맞게 구현
     *
     * @param link 기사 링크
     * @return 추출된 고유 ID
     * @throws ArticleCollectorException 링크가 null이거나 ID를 추출할 수 없는 경우
     */
    protected abstract String extractUniqueIdFromLink(String link);

    private ArticleEntity buildArticleEntity(String title, String link, LocalDateTime pubDate,
                                             String guid, String description, String category, String imageUrl) {
        return ArticleEntity.builder()
                .title(title)
                .link(link)
                .pubDate(pubDate)
                .category(category)
                .guid(guid)
                .description(description)
                .imageUrl(imageUrl)
                .build();
    }

    /**
     * 링크가 null이거나 비어있는지 검증
     *
     * @param link 검증할 링크
     * @throws ArticleCollectorException 링크가 유효하지 않을 경우
     */
    private void validateEntryLink(String link) {
        if (isNullOrEmpty(link)) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.ARTICLE_ID_EXTRACTION_ERROR);
        }
    }

    /**
     * 고유 ID가 null이거나 비어있는지 검증
     *
     * @param uniqueId 검증할 고유 ID
     * @throws ArticleCollectorException 고유 ID가 유효하지 않을 경우
     */
    private void validateUniqueId(String uniqueId) {
        if (isNullOrEmpty(uniqueId)) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.ARTICLE_ID_EXTRACTION_ERROR);
        }
    }

    /**
     * 문자열이 null이거나 비어있는지 확인
     *
     * @param str 확인할 문자열
     * @return null이거나 비어있으면 true, 그렇지 않으면 false
     */
    private boolean isNullOrEmpty(String str) {
        return null == str || str.trim().isEmpty();
    }

    /**
     * 리스트가 null이거나 비어있는지 확인
     *
     * @param list 확인할 리스트
     * @return null이거나 비어있으면 true, 그렇지 않으면 false
     */
    private boolean isNullOrEmptyList(List<?> list) {
        return null == list || list.isEmpty();
    }
}