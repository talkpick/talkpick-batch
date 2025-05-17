package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.implement;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.batch.RssSource;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.AbstractRssMapper;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.result.ScrapingResult;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.factory.ScraperFactory;
import com.rometools.rome.feed.synd.SyndEntry;

import groovy.util.logging.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 국민일보 RSS 매퍼 구현체
 * HTML 태그를 제거하고 문단을 PARAGRAPH_BREAK로 구분하여 반환한다.
 *
 * @author 양병학
 * @since 2025-05-10 최초 작성
 * @modified 2025-05-15 템플릿 메서드 패턴 적용, 의존성 주입 방식 개선
 * @modified 2025-05-17 HTML 태그 제거 및 문단 구분 기능 추가
 */
@Slf4j
@Component
public class KmibRssMapper extends AbstractRssMapper {

    private static final Pattern ARCID_PATTERN = Pattern.compile("arcid=([0-9]+)");
    private static final Pattern IMG_SRC_PATTERN = Pattern.compile("<img\\s+src=[\"']([^\"']+)[\"']");

    private final ScraperFactory scraperFactory;

    @Autowired
    public KmibRssMapper(ScraperFactory scraperFactory) {
        this.scraperFactory = scraperFactory;
    }

    /**
     * 템플릿 메서드 패턴에서 사용할 ScraperFactory 반환
     *
     * @return 주입받은 ScraperFactory 인스턴스
     * @since 2025-05-15
     */
    @Override
    protected ScraperFactory getScraperFactory() {
        return this.scraperFactory;
    }

    /**
     * 매퍼 타입 반환
     *
     * @return 매퍼 타입 (km)
     * @since 2025-05-10
     */
    @Override
    public String getMapperType() {
        return "km";
    }

    @Override
    protected ScrapingResult performSpecificMapping(
            SyndEntry entry,
            RssSource source,
            String link,
            String baseDescription,
            String baseImageUrl) {

        // KmibRssMapper는 RSS 데이터만 사용하되, HTML 태그 제거 및 문단 처리
        String processedDescription = processDescription(entry);

        // 이미지 URL이 없으면 description에서 추출 시도
        String finalImageUrl = baseImageUrl.isEmpty()
                ? extractImageFromDescription(entry)
                : baseImageUrl;

        return new ScrapingResult(processedDescription, finalImageUrl);
    }

    private String processDescription(SyndEntry entry) {
        if (entry.getDescription() == null) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.RSS_CONTENT_EMPTY);
        }

        String rawDescription = entry.getDescription().getValue();
        if (rawDescription == null || rawDescription.isEmpty()) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.RSS_CONTENT_EMPTY);
        }

        List<String> paragraphs = extractCleanParagraphs(rawDescription);
        if (paragraphs.isEmpty()) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.RSS_CONTENT_EMPTY);
        }

        return serializeParagraphs(paragraphs);
    }

    /**
     * 링크에서 arcid 값 추출
     *
     * @param link 기사 링크
     * @return 추출된 arcid
     * @throws ArticleCollectorException 링크가 null이거나 arcid 추출 실패 시
     * @since 2025-05-10
     */
    @Override
    protected String extractUniqueIdFromLink(String link) {
        if (link == null || link.trim().isEmpty()) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.ARTICLE_ID_EXTRACTION_ERROR);
        }

        Matcher matcher = ARCID_PATTERN.matcher(link);
        if (!matcher.find()) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.ARTICLE_ID_EXTRACTION_ERROR);
        }

        String arcId = matcher.group(1);
        if (arcId == null || arcId.trim().isEmpty()) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.ARTICLE_ID_EXTRACTION_ERROR);
        }

        return arcId;
    }

    /**
     * 이미지 URL 추출 메서드
     * 국민일보 RSS feed는 media:content 태그 대신 description의 HTML 내 img 태그에서 이미지 URL 추출
     *
     * @param entry RSS 항목
     * @return 이미지 URL
     * @since 2025-05-10
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
     * @since 2025-05-10
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
     * RSS description에서 HTML 태그를 제거하고 문단을 추출하여 PARAGRAPH_BREAK로 구분
     *
     * @param entry RSS 항목
     * @return PARAGRAPH_BREAK로 구분된 문단 텍스트
     * @since 2025-05-10
     * @modified 2025-05-17 HTML 태그 제거 및 문단 구분 기능 추가
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
}