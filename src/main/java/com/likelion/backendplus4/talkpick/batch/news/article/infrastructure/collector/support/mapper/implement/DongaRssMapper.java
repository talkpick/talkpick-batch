package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.implement;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.batch.RssSource;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.AbstractRssMapper;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.result.ScrapingResult;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.ContentScraper;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.factory.ScraperFactory;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;
import com.rometools.rome.feed.synd.SyndEntry;

import groovy.util.logging.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 동아일보 RSS 매퍼 구현체
 * HTML에서 문단을 추출하고 PARAGRAPH_BREAK로 구분하여 반환한다.
 *
 * @author 양병학
 * @since 2025-05-10 최초 작성
 * @modified 2025-05-15 템플릿 메서드 패턴 적용, 의존성 주입 방식 개선
 * @modified 2025-05-17 HTML 태그 제거 및 문단 구분 기능 추가
 */
@Slf4j
@Component
public class DongaRssMapper extends AbstractRssMapper {

    private final ScraperFactory scraperFactory;

    @Autowired
    public DongaRssMapper(ScraperFactory scraperFactory) {
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
     * @return 매퍼 타입 (da)
     * @since 2025-05-10
     */
    @Override
    public String getMapperType() {
        return "da";
    }

    /**
     * 카테고리 정보 추출
     *
     * @param entry RSS 항목
     * @param source RSS 소스 정보
     * @return 카테고리
     * @since 2025-05-10
     */
    @Override
    protected String extractCategory(SyndEntry entry, RssSource source) {
        return source.getCategoryName();
    }

    /**
     * 본문만 스크래핑 하는 메소드
     *
     * @return ScrapingResult 객체 (스크래핑 정보)
     * @since 2025-05-17
     */
    @Override
    protected ScrapingResult performSpecificMapping(
            SyndEntry entry,
            RssSource source,
            String link,
            String baseDescription,
            String baseImageUrl) {

        String scrapedContent = scrapeContent(link);

        return new ScrapingResult(scrapedContent, baseImageUrl);
    }

    /**
     * 본문 스크래핑을 진행하는 메소드
     *
     * @return 스크래핑된 본문 String
     * @author 양병학
     * @since 2025-05-17
     */
    private String scrapeContent(String link) {
        ContentScraper scraper = getScraperOrThrow();
        String scrapedContent = scrapeAndValidateContent(scraper, link);
        return removeUnwantedPhrases(scrapedContent);
    }

    private ContentScraper getScraperOrThrow() {
        return getScraperFactory().getScraper(getMapperType())
                .orElseThrow(() -> new ArticleCollectorException(ArticleCollectorErrorCode.SCRAPER_NOT_FOUND));
    }

    private String scrapeAndValidateContent(ContentScraper scraper, String link) {
        String scrapedContent = scraper.scrapeContent(link);
        validateScrapedContent(scrapedContent);
        return scrapedContent;
    }

    private void validateScrapedContent(String content) {
        Optional.ofNullable(content)
                .filter(c -> !c.isEmpty())
                .orElseThrow(() -> new ArticleCollectorException(ArticleCollectorErrorCode.EMPTY_ARTICLE_CONTENT));
    }

    /**
     * 동아일보 링크에서 고유 ID 추출
     *
     * @param link 기사 링크
     * @return 추출된 고유 ID
     * @throws ArticleCollectorException 링크가 null이거나 ID를 추출할 수 없는 경우
     * @since 2025-05-10
     */
    @Override
    protected String extractUniqueIdFromLink(String link) {
        validateLink(link);
        return extractIdFromParts(link);
    }

    private void validateLink(String link) {
        if (null == link || link.trim().isEmpty()) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.ARTICLE_ID_EXTRACTION_ERROR);
        }
    }

    private String extractIdFromParts(String link) {
        String[] parts = link.split("/");
        if (parts.length >= 2) {
            String id = parts[parts.length - 2];
            if (isValidId(id)) {
                return id;
            }
        }

        throw new ArticleCollectorException(ArticleCollectorErrorCode.ARTICLE_ID_EXTRACTION_ERROR);
    }

    private boolean isValidId(String id) {
        return id != null && !id.trim().isEmpty();
    }

    /**
     * 불용어 제거 메서드
     * 저작권 문구, 광고 문구 등 불필요한 문구 제거
     *
     * @param content 원본 내용
     * @return 불용어가 제거된 내용
     * @since 2025-05-17
     */
    private String removeUnwantedPhrases(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        content = content.replaceAll("\\(c\\)\\s*동아일보", "");
        content = content.replaceAll("저작권자.*동아일보.*무단.*전재.*금지", "");
        content = content.replaceAll("무단전재 및 재배포 금지", "");
        content = content.replaceAll("\\S+기자\\s+\\S+@donga\\.com", "");
        content = content.replaceAll("동아닷컴 뉴스스탠드", "");
        content = content.replaceAll("동아일보 홈페이지", "");
        content = content.replaceAll("PARAGRAPH_BREAKPARAGRAPH_BREAK", "PARAGRAPH_BREAK");

        return content.trim();
    }
}