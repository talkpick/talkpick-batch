package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.batch.RssSource;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.result.ScrapingResult;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.ContentScraper;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.factory.ScraperFactory;
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
    private static final ObjectMapper objectMapper = new ObjectMapper();

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
        // 1. 메타데이터 추출
        String guid = extractGuid(entry, source);
        String title = extractTitle(entry);
        String link = extractLink(entry);
        LocalDateTime pubDate = extractPubDate(entry);
        String category = extractCategory(entry, source);

        // 2. 기본 이미지 URL 추출 (RSS에서)
        String imageUrl = extractImageUrl(entry);

        // 3. 기본 설명 추출 (RSS에서)
        String baseDescription = extractDescription(entry);

        // 4. 매퍼 유형에 따른 처리 (자식 클래스에서 구현)
        ScrapingResult result = performSpecificMapping(entry, source, link, baseDescription, imageUrl);

        // 5. ArticleEntity 생성 및 반환
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
     * HTML 문자열에서 모든 태그를 제거하고 문단을 추출하는 공통 메서드
     *
     * @param html HTML 문자열
     * @return 정제된 문단 리스트
     */
    protected List<String> extractCleanParagraphs(String html) {
        if (isNullOrEmpty(html)) {
            return new ArrayList<>();
        }

        try {
            String withBreaks = html.replaceAll("<br\\s*/?>", "PARAGRAPH_BREAK");
            String noTags = withBreaks.replaceAll("<[^>]*>", "");
            String decoded = noTags.replace("&nbsp;", " ")
                    .replace("&#160;", " ")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&amp;", "&")
                    .replace("&quot;", "\"")
                    .replace("&apos;", "'");

            decoded = decoded.replaceAll("\\s+", " ").trim();
            String[] paragraphs = decoded.split("PARAGRAPH_BREAK");

            return Arrays.stream(paragraphs)
                    .map(String::trim)
                    .filter(p -> !p.isEmpty())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            List<String> fallback = new ArrayList<>();
            fallback.add(removeAllHtmlTags(html));
            return fallback;
        }
    }

    /**
     * 모든 HTML 태그 제거하는 공통 메서드
     *
     * @param html HTML 문자열
     * @return 태그가 제거된 문자열
     */
    protected String removeAllHtmlTags(String html) {
        if (isNullOrEmpty(html)) {
            return "";
        }

        String noTags = html.replaceAll("<[^>]*>", "");
        String decoded = decodeHtmlEntities(noTags);

        return decoded.replaceAll("\\s+", " ").trim();
    }

    /**
     * HTML 엔티티를 디코딩하는 유틸리티 메서드
     *
     * @param text HTML 엔티티가 포함된 문자열
     * @return 디코딩된 문자열
     */
    protected String decodeHtmlEntities(String text) {
        if (isNullOrEmpty(text)) {
            return "";
        }

        return text.replace("&nbsp;", " ")
                .replace("&#160;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'");
    }

    /**
     * 문단 리스트를 JSON으로 직렬화하는 공통 메서드
     *
     * @param paragraphs 문단 리스트
     * @return JSON 문자열
     */
    protected String serializeParagraphs(List<String> paragraphs) {
        if (isNullOrEmptyList(paragraphs)) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paragraphs.size(); i++) {
            if (i > 0) {
                sb.append("PARAGRAPH_BREAK");
            }
            sb.append(paragraphs.get(i));
        }

        return sb.toString();
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