package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.implement;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.batch.RssSource;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.AbstractRssMapper;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.result.ScrapingResult;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.factory.ScraperFactory;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.util.HtmlParser;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.util.ParagraphUtil;
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
    private final HtmlParser htmlParser;
    private final ParagraphUtil paragraphUtil;

    @Autowired
    public KmibRssMapper(ScraperFactory scraperFactory,
                         HtmlParser htmlParser,
                         ParagraphUtil paragraphUtil) {
        this.scraperFactory = scraperFactory;
        this.htmlParser = htmlParser;
        this.paragraphUtil = paragraphUtil;
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

        String processedDescription = processDescription(entry);

        String finalImageUrl = baseImageUrl.isEmpty()
                ? extractImageFromDescription(entry)
                : baseImageUrl;

        return new ScrapingResult(processedDescription, finalImageUrl);
    }

    /**
     * 엔트리의 description을 처리하여 문단으로 변환합니다.
     *
     * 1. 설명 존재 여부 검증
     * 2. 원시 설명 텍스트 추출 및 검증
     * 3. HTML 태그 제거 및 문단 분리
     * 4. 문단 목록 검증
     * 5. 문단을 PARAGRAPH_BREAK로 구분하여 직렬화
     *
     * @param entry 처리할 RSS 항목
     * @return PARAGRAPH_BREAK로 구분된 문단 텍스트
     * @throws ArticleCollectorException 설명이 없거나 빈 경우, 문단이 없는 경우 발생
     * @since 2025-05-18
     * @author 양병학
     */
    private String processDescription(SyndEntry entry) {
        validateDescriptionExists(entry);

        String rawDescription = entry.getDescription().getValue();
        validateRawDescription(rawDescription);

        List<String> paragraphs = htmlParser.extractCleanParagraphs(rawDescription);
        validateParagraphs(paragraphs);

        return paragraphUtil.serializeParagraphs(paragraphs);
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
        validateLink(link);

        String arcId = extractArcIdFromLink(link);
        validateArcId(arcId);

        return arcId;
    }

    private void validateLink(String link) {
        if (null == link || link.trim().isEmpty()) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.ARTICLE_ID_EXTRACTION_ERROR);
        }
    }

    private void validateArcId(String arcId) {
        if (null == arcId || arcId.trim().isEmpty()) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.ARTICLE_ID_EXTRACTION_ERROR);
        }
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

    private String extractImageFromDescription(SyndEntry entry) {
        if (isEntryDescriptionEmpty(entry)) {
            return "";
        }

        String description = entry.getDescription().getValue();
        if (isNullOrEmpty(description)) {
            return "";
        }

        return extractImageUrlFromHtml(description);
    }

    /**
     * HTML에서 이미지 URL을 추출합니다.
     *
     * @param html 이미지 URL을 추출할 HTML 문자열
     * @return 추출된 이미지 URL 또는 빈 문자열
     * @since 2025-05-18
     * @author 양병학
     */
    private String extractImageUrlFromHtml(String html) {
        Matcher matcher = IMG_SRC_PATTERN.matcher(html);
        return matcher.find() ? matcher.group(1) : "";
    }

    /**
     * RSS description에서 HTML 태그를 제거하고 문단을 추출하여 PARAGRAPH_BREAK로 구분
     *
     * 1. 설명 컨텐츠 존재 여부 확인
     * 2. HTML 태그 제거 및 문단 분리
     * 3. 문단을 PARAGRAPH_BREAK로 구분하여 반환
     *
     * @param entry RSS 항목
     * @return PARAGRAPH_BREAK로 구분된 문단 텍스트
     * @throws ArticleCollectorException 설명이 비어있거나 파싱 중 오류 발생 시
     * @since 2025-05-10
     * @author 양병학
     * @modified 2025-05-17 HTML 태그 제거 및 문단 구분 기능 추가
     * @modified 2025-05-18 예외 처리 로직 개선
     */
    @Override
    protected String extractDescription(SyndEntry entry) {
        validateDescriptionExists(entry);

        String rawDescription = entry.getDescription().getValue();
        validateRawDescription(rawDescription);

        try {
            List<String> paragraphs = htmlParser.extractCleanParagraphs(rawDescription);
            validateParagraphs(paragraphs);
            return paragraphUtil.serializeParagraphs(paragraphs);
        } catch (Exception e) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.RSS_PARSING_ERROR, e);
        }
    }

    private void validateDescriptionExists(SyndEntry entry) {
        if (null == entry.getDescription()) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.RSS_CONTENT_EMPTY);
        }
    }

    private void validateRawDescription(String rawDescription) {
        if (null == rawDescription || rawDescription.isEmpty()) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.RSS_CONTENT_EMPTY);
        }
    }

    private void validateParagraphs(List<String> paragraphs) {
        if (paragraphs.isEmpty()) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.RSS_CONTENT_EMPTY);
        }
    }

    /**
     * 링크에서 arcId를 추출합니다.
     *
     * @param link arcId를 추출할 링크
     * @return 추출된 arcId
     * @throws ArticleCollectorException arcId를 추출할 수 없는 경우 발생
     * @since 2025-05-18
     * @author 양병학
     */
    private String extractArcIdFromLink(String link) {
        Matcher matcher = ARCID_PATTERN.matcher(link);
        if (!matcher.find()) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.ARTICLE_ID_EXTRACTION_ERROR);
        }
        return matcher.group(1);
    }

    private boolean isEntryDescriptionEmpty(SyndEntry entry) {
        return entry.getDescription() == null;
    }

    /**
     * 문자열이 null이거나 비어있는지 확인합니다.
     *
     * @param str 확인할 문자열
     * @return null이거나 비어있으면 true, 그렇지 않으면 false
     */
    private boolean isNullOrEmpty(String str) {
        return null == str || str.isEmpty();
    }


}