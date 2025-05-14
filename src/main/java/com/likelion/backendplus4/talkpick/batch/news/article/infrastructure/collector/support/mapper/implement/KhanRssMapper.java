package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.implement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.batch.RssSource;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.AbstractRssMapper;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.ContentScraper;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.factory.ScraperFactory;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;
import com.rometools.rome.feed.synd.SyndEntry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 경향신문 RSS 매퍼 구현체
 *
 * @author 양병학
 * @since 2025-05-10 최초 작성
 * @modified 2025-05-15 템플릿 메서드 패턴 적용, 의존성 주입 방식 개선
 * @modified 2025-05-17 mapToRssNews 메서드 오버라이드 및 문단 직렬화 기능 추가
 */
@Component
public class KhanRssMapper extends AbstractRssMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ScraperFactory scraperFactory;

    @Autowired
    public KhanRssMapper(ScraperFactory scraperFactory) {
        this.scraperFactory = scraperFactory;
    }

    /**
     * 템플릿 메서드 패턴
     *
     * @return 주입받은 ScraperFactory 인스턴스
     */
    @Override
    protected ScraperFactory getScraperFactory() {
        return this.scraperFactory;
    }

    /**
     * RSS 피드를 ArticleEntity 엔티티로 변환 (오버라이드)
     * 경향신문 특화 구현 - 본문과 이미지 URL을 효율적으로 스크래핑하고 문단 직렬화
     *
     * @param entry 변환할 SyndEntry(Rss 데이터) 객체
     * @param source RSS 소스 정보
     * @return 변환된 ArticleEntity 엔티티
     */
    @Override
    public ArticleEntity mapToRssNews(SyndEntry entry, RssSource source) {
        String title = extractTitle(entry);
        String link = extractLink(entry);
        LocalDateTime pubDate = extractPubDate(entry);
        String guid = extractGuid(entry, source);
        String description = extractDescription(entry);
        String category = extractCategory(entry, source);
        String imageUrl = super.extractImageUrl(entry);

        ContentResult contentResult;
        if (source.hasFullContent()) {
            contentResult = new ContentResult(description, imageUrl);
        } else {
            contentResult = scrapeContentAndImage(link, description, imageUrl);
        }

        return ArticleEntity.builder()
                .title(title)
                .link(link)
                .pubDate(pubDate)
                .category(category)
                .guid(guid)
                .description(contentResult.getContent())
                .imageUrl(contentResult.getImageUrl())
                .build();
    }

    /**
     * 본문과 이미지 URL을 스크래핑하고 처리하는 메서드
     *
     * @param link 기사 URL
     * @param fallbackDescription 스크래핑 실패 시 사용할 설명
     * @param fallbackImageUrl 스크래핑 실패 시 사용할 이미지 URL
     * @return 처리된 콘텐츠와 이미지 URL이 포함된 결과 객체
     */
    private ContentResult scrapeContentAndImage(String link, String fallbackDescription, String fallbackImageUrl) {
        try {
            ContentScraper scraper = getScraperFactory().getScraper(getMapperType())
                    .orElseThrow(() -> new ArticleCollectorException(ArticleCollectorErrorCode.MAPPER_NOT_FOUND));

            String content = fallbackDescription;
            String scrapedContent = scraper.scrapeContent(link);
            if (scrapedContent != null && !scrapedContent.isEmpty()) {
                List<String> paragraphs = Arrays.asList(scrapedContent.split("\n\n"));
                content = serializeParagraphs(paragraphs);
            }

            String imageUrl = fallbackImageUrl;
            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = scraper.scrapeImageUrl(link);
            }

            return new ContentResult(content, imageUrl);
        } catch (Exception e) {
            System.err.println("경향신문 스크래핑 실패: " + e.getMessage());
            return new ContentResult(fallbackDescription, fallbackImageUrl);
        }
    }

    /**
     * 콘텐츠 결과를 담는 내부 클래스
     */
    private static class ContentResult {
        private final String content;
        private final String imageUrl;

        public ContentResult(String content, String imageUrl) {
            this.content = content;
            this.imageUrl = imageUrl;
        }

        public String getContent() {
            return content;
        }

        public String getImageUrl() {
            return imageUrl;
        }
    }

    /**
     * 매퍼 타입 반환
     *
     * @return 매퍼 타입 (kh)
     */
    @Override
    public String getMapperType() {
        return "kh";
    }

    /**
     * GUID 추출, 링크에서 기사 ID를 추출하여 사용
     *
     * @param entry RSS 항목
     * @param source RSS 소스 정보
     * @return 신문사 코드 + 기사 ID 형태의 GUID
     */
    @Override
    protected String extractGuid(SyndEntry entry, RssSource source) {
        String uniqueId = extractUniqueIdFromLink(entry.getLink());
        return source.getCodePrefix() + uniqueId;
    }

    /**
     * 경향신문 링크에서 고유 ID 추출
     *
     * @param link 기사 링크
     * @return 추출된 고유 ID
     * @throws ArticleCollectorException 링크가 null이거나 ID를 추출할 수 없는 경우
     */
    private String extractUniqueIdFromLink(String link) {
        if (link == null || link.trim().isEmpty()) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.ITEM_MAPPING_ERROR);
        }

        try {
            String[] parts = link.split("/");
            for (int i = 0; i < parts.length; i++) {
                if ("article".equals(parts[i]) && i + 1 < parts.length) {
                    String id = parts[i + 1];
                    if (id != null && !id.trim().isEmpty()) {
                        return id;
                    }
                }
            }
        } catch (Exception e) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.ITEM_MAPPING_ERROR, e);
        }

        throw new ArticleCollectorException(ArticleCollectorErrorCode.ITEM_MAPPING_ERROR);
    }

    /**
     * 발행일 추출, 경향신문은 dc:date 태그 확인
     *
     * @param entry RSS 항목
     * @return 발행일 LocalDateTime
     */
    @Override
    protected LocalDateTime extractPubDate(SyndEntry entry) {
        if (entry.getPublishedDate() != null) {
            return convertToLocalDateTime(entry.getPublishedDate());
        }

        return extractDcDate(entry);
    }

    /**
     * date 태그에서 발행일 추출
     *
     * @param entry RSS 항목
     * @return 추출된 발행일, 없으면 현재 시간
     */
    private LocalDateTime extractDcDate(SyndEntry entry) {
        return entry.getForeignMarkup().stream()
                .filter(element -> "date".equals(element.getName()) &&
                        "dc".equals(element.getNamespacePrefix()))
                .findFirst()
                .map(element -> parseDateTime(element.getValue()))
                .orElse(LocalDateTime.now());
    }

    /**
     * 문자열을 LocalDateTime으로 파싱
     *
     * @param dateString 날짜 문자열
     * @return 파싱된 LocalDateTime, 실패 시 현재 시간
     */
    private LocalDateTime parseDateTime(String dateString) {
        try {
            return LocalDateTime.parse(dateString);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    /**
     * 카테고리 정보 추출
     *
     * @param entry RSS 항목
     * @param source RSS 소스 정보
     * @return 결합된 카테고리 문자열
     */
    @Override
    protected String extractCategory(SyndEntry entry, RssSource source) {
        return source.getCategoryName();
    }

    /**
     * 문단 리스트를 JSON으로 직렬화
     *
     * @param paragraphs 문단 리스트
     * @return JSON 문자열
     */
    private String serializeParagraphs(List<String> paragraphs) {
        try {
            return objectMapper.writeValueAsString(paragraphs);
        } catch (JsonProcessingException e) {
            return String.join("\n\n", paragraphs);
        }
    }

    /**
     * HTML 문자열에서 모든 태그를 제거하고 문단을 추출하는 메서드
     *
     * @param html HTML 문자열
     * @return 정제된 문단 리스트
     */
    private List<String> extractCleanParagraphs(String html) {
        if (html == null || html.isEmpty()) {
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
     * 모든 HTML 태그 제거
     *
     * @param html HTML 문자열
     * @return 태그가 제거된 문자열
     */
    private String removeAllHtmlTags(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }

        String noTags = html.replaceAll("<[^>]*>", "");
        String decoded = noTags.replace("&nbsp;", " ")
                .replace("&#160;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'");

        return decoded.replaceAll("\\s+", " ").trim();
    }
}