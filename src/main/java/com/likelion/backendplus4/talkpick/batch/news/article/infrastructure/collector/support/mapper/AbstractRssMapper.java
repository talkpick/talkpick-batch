package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.batch.RssSource;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.ContentScraper;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.factory.ScraperFactory;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

/**
 * RSS를 ArticleEntity로 변환하는 추상 클래스
 * 공통 변환 로직을 제공
 *
 * @author 양병학
 * @since 2025-05-13 최초 작성
 */
public abstract class AbstractRssMapper {

    @Autowired
    private ScraperFactory scraperFactory;

    /**
     * RSS 피드를 ArticleEntity 엔티티로 변환
     *
     * @param entry 변환할 SyndEntry(Rss 데이터) 객체
     * @param source RSS 소스 정보
     * @return 변환된 ArticleEntity 엔티티
     */
    public ArticleEntity mapToRssNews(SyndEntry entry, RssSource source) {
        String title = extractTitle(entry);
        String link = extractLink(entry);
        LocalDateTime pubDate = extractPubDate(entry);
        String guid = extractGuid(entry, source);
        String description = extractDescription(entry);
        String category = extractCategory(entry, source);

        String content = getContentWithScraping(description, link, source.getMapperType());

        return buildArticleEntity(title, link, pubDate, guid, description, category);
    }

    /**
     * 매퍼의 유형을 식별하는 코드 반환
     * 소문자 언론사 코드 형태 (예: "km", "da", "kh")
     */
    public abstract String getMapperType();

    /**
     * Date 객체를 LocalDateTime으로 변환
     *
     * @param date 변환할 Date 객체
     * @return 변환된 LocalDateTime 객체, date가 null이면 현재 시간 반환
     */
    protected LocalDateTime convertToLocalDateTime(Date date) {
        return date != null
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
        return entry.getDescription() != null ? entry.getDescription().getValue() : null;
    }

    /**
     * 본문 내용을 가져오는 메서드
     * 스크래퍼가 있으면 스크래핑을 시도하고, 실패하면 원본 description 사용
     *
     * @param originalDescription RSS에서 추출한 기본 설명
     * @param link 기사 URL
     * @param mapperType 매퍼 타입
     * @return 최종 본문 내용
     */
    private String getContentWithScraping(String originalDescription, String link, String mapperType) {
        // 스크래퍼가 있으면 스크래핑 시도
        Optional<ContentScraper> scraper = scraperFactory.getScraper(mapperType);
        if (scraper.isPresent()) {
            try {
                String scrapedContent = scraper.get().scrapeContent(link);
                if (scrapedContent != null && !scrapedContent.isEmpty()) {
                    return scrapedContent;
                }
            } catch (Exception e) {
                throw new ArticleCollectorException(ArticleCollectorErrorCode.FEED_PARSING_ERROR, e);
            }
        }

        // 스크래퍼가 없거나 스크래핑 실패 시 기존 description 반환
        return originalDescription;
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
    protected abstract String extractGuid(SyndEntry entry, RssSource source);

    /**
     * 뉴스 요약 여부 반환
     * Default true를 반환, false 사용시 각 뉴스 Mapper에서 오버라이드해야 함
     *
     * @return 요약본 여부
     */
    protected boolean getIsSummary() {
        return true;
    }

    private ArticleEntity buildArticleEntity(String title, String link, LocalDateTime pubDate,
                                             String guid, String description, String category) {
        return ArticleEntity.builder()
                .title(title)
                .link(link)
                .pubDate(pubDate)
                .category(category)
                .guid(guid)
                .description(description)
                .isSummary(getIsSummary())
                .build();
    }


}