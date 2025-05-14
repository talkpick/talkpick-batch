package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.implement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.batch.RssSource;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.AbstractRssMapper;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.factory.ScraperFactory;
import com.rometools.rome.feed.synd.SyndEntry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 동아일보 RSS 매퍼 구현체
 *
 * @author 양병학
 * @since 2025-05-10 최초 작성
 * @modified 2025-05-15 템플릿 메서드 패턴 적용, 의존성 주입 방식 개선
 * @modified 2025-05-17 HTML 태그 제거 및 문단 직렬화 기능 추가
 */
@Component
public class DongaRssMapper extends AbstractRssMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ScraperFactory scraperFactory;

    @Autowired
    public DongaRssMapper(ScraperFactory scraperFactory) {
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
     * 매퍼 타입 반환
     *
     * @return 매퍼 타입 (da)
     */
    @Override
    public String getMapperType() {
        return "da";
    }

    /**
     * GUID 추출, URI를 GUID로 사용
     *
     * @param entry RSS 항목
     * @param source RSS 소스 정보
     * @return URI 또는 생성된 고유 ID
     */
    @Override
    protected String extractGuid(SyndEntry entry, RssSource source) {
        String uniqueId = extractUniqueIdFromLink(entry.getLink());
        return source.getCodePrefix() + uniqueId;
    }

    /**
     * 동아일보 링크에서 고유 ID 추출
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
            if (parts.length >= 2) {
                return parts[parts.length - 2];
            }
        } catch (Exception e) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.ITEM_MAPPING_ERROR, e);
        }

        throw new ArticleCollectorException(ArticleCollectorErrorCode.ITEM_MAPPING_ERROR);
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
     * RSS description에서 HTML 태그를 제거하고 문단을 추출하여 직렬화
     *
     * @param entry RSS 항목
     * @return 직렬화된 문단 JSON 또는 원본 description
     */
    @Override
    protected String extractDescription(SyndEntry entry) {
        if (entry.getDescription() == null) {
            return "";
        }

        String rawDescription = entry.getDescription().getValue();
        if (rawDescription == null || rawDescription.isEmpty()) {
            return "";
        }

        return processHtmlContent(rawDescription);
    }

    /**
     * HTML 콘텐츠 처리하여 정제된 문단 직렬화
     *
     * @param htmlContent HTML 콘텐츠
     * @return 직렬화된 문단 JSON 또는 태그가 제거된 텍스트
     */
    private String processHtmlContent(String htmlContent) {
        try {
            List<String> paragraphs = extractCleanParagraphs(htmlContent);
            return serializeParagraphs(paragraphs);
        } catch (Exception e) {
            return removeAllHtmlTags(htmlContent);
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
}