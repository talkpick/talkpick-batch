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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 국민일보 RSS 매퍼 구현체
 *
 * @author 양병학
 * @since 2025-05-10 최초 작성
 * @modified 2025-05-15 템플릿 메서드 패턴 적용, 의존성 주입 방식 개선
 * @modified 2025-05-17 HTML 태그 제거 및 문단 직렬화 기능 추가
 */
@Component
public class KmibRssMapper extends AbstractRssMapper {

    private static final Pattern ARCID_PATTERN = Pattern.compile("arcid=([0-9]+)");
    private static final Pattern IMG_SRC_PATTERN = Pattern.compile("""
    <img\\s+src=["']([^"']+)["']
    """.trim());

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ScraperFactory scraperFactory;

    @Autowired
    public KmibRssMapper(ScraperFactory scraperFactory) {
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
     * @return 매퍼 타입 (km)
     */
    @Override
    public String getMapperType() {
        return "km";
    }

    /**
     * GUID 추출, 링크에서 arcid를 추출하여 생성
     *
     * @param entry RSS 항목
     * @param source RSS 소스 정보
     * @return 형식: [언론사코드][arcid]
     */
    @Override
    protected String extractGuid(SyndEntry entry, RssSource source) {
        if (entry.getLink() == null || entry.getLink().trim().isEmpty()) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.ITEM_MAPPING_ERROR);
        }

        String arcId = extractArcIdFromLink(entry.getLink());
        if (arcId.trim().isEmpty()) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.ITEM_MAPPING_ERROR);
        }

        return source.getCodePrefix() + arcId;
    }

    /**
     * 링크에서 arcid 값 추출
     *
     * @param link 기사 링크
     * @return 추출된 arcid, 없으면 타임스탬프 반환
     */
    private String extractArcIdFromLink(String link) {
        if (link == null || link.trim().isEmpty()) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.ITEM_MAPPING_ERROR);
        }

        Matcher matcher = ARCID_PATTERN.matcher(link);
        if (matcher.find()) {
            String arcId = matcher.group(1);
            if (arcId != null && !arcId.trim().isEmpty()) {
                return arcId;
            }
        }

        throw new ArticleCollectorException(ArticleCollectorErrorCode.ITEM_MAPPING_ERROR);
    }

    /**
     * 이미지 URL 추출 메서드
     * 국민일보 RSS feed는 media:content 태그 대신 description의 HTML 내 img 태그에서 이미지 URL 추출
     *
     * @param entry RSS 항목
     * @return 이미지 URL
     */
    @Override
    protected String extractImageUrl(SyndEntry entry) {
        String mediaContent = super.extractImageUrl(entry);
        if (!mediaContent.isEmpty()) {
            return mediaContent;
        }

        return extractImageFromDescription(entry);
    }

    /**
     * Description 내용에서 이미지 URL을 추출
     *
     * @param entry RSS 항목
     * @return 추출된 이미지 URL 또는 빈 문자열
     */
    private String extractImageFromDescription(SyndEntry entry) {
        if (entry.getDescription() == null) {
            return "";
        }

        String description = entry.getDescription().getValue();
        if (description == null || description.isEmpty()) {
            return "";
        }

        Matcher matcher = IMG_SRC_PATTERN.matcher(description);
        return matcher.find() ? matcher.group(1) : "";
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

        try {
            List<String> paragraphs = extractCleanParagraphs(rawDescription);
            return serializeParagraphs(paragraphs);
        } catch (Exception e) {
            return removeAllHtmlTags(rawDescription);
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