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

import java.io.IOException;
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
 * @modified 2025-05-15 의존성 주입 방식 개선 (템플릿 메서드 패턴 적용)
 */
public abstract class AbstractRssMapper {

    protected abstract ScraperFactory getScraperFactory();

    /**
     * RSS 피드를 ArticleEntity 엔티티로 변환
     *
     * @param entry 변환할 SyndEntry(Rss 데이터) 객체
     * @param source RSS 소스 정보
     * @return 변환된 ArticleEntity 엔티티
     */
    public ArticleEntity mapToRssNews(SyndEntry entry, RssSource source) {
        ArticleInfo info = extractBasicInfo(entry, source);

        String content = determineContent(info.description, info.link, source);

        return buildArticleEntity(
                info.title,
                info.link,
                info.pubDate,
                info.guid,
                content,
                info.category,
                info.imageUrl);
    }

    /**
     * RSS 항목에서 기본 정보 추출
     */
    private ArticleInfo extractBasicInfo(SyndEntry entry, RssSource source) {
        return new ArticleInfo(
                extractTitle(entry),
                extractLink(entry),
                extractPubDate(entry),
                extractGuid(entry, source),
                extractDescription(entry),
                extractCategory(entry, source),
                extractImageUrl(entry)
        );
    }

    /**
     * 본문 내용 결정 (RSS 또는 스크래핑)
     */
    private String determineContent(String description, String link, RssSource source) {
        if (source.hasFullContent()) {
            return description;
        }

        return getContentWithScraping(description, link, source.getMapperType());
    }

    /**
     * 기사 기본 정보를 담는 내부 클래스
     */
    private record ArticleInfo(
            String title,
            String link,
            LocalDateTime pubDate,
            String guid,
            String description,
            String category,
            String imageUrl
    ) {}

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
     * 본문 내용을 가져오는 메서드
     *
     * @param originalDescription RSS에서 추출한 기본 설명
     * @param link 기사 URL
     * @param mapperType 매퍼 타입
     * @return 최종 본문 내용
     */
    private String getContentWithScraping(String originalDescription, String link, String mapperType) {
        ContentScraper scraper = findScraper(mapperType);
        return scrapeContent(scraper, link, originalDescription);
    }

    /**
     * 매퍼 타입에 맞는 스크래퍼를 찾음
     *
     * @param mapperType 매퍼 타입
     * @return 스크래퍼 객체
     * @throws ArticleCollectorException 스크래퍼를 찾을 수 없는 경우
     */
    private ContentScraper findScraper(String mapperType) {
        return getScraperFactory().getScraper(mapperType)
                .orElseThrow(() -> new ArticleCollectorException(ArticleCollectorErrorCode.MAPPER_NOT_FOUND));
    }

    /**
     * 스크래퍼를 사용하여 콘텐츠 스크래핑 수행
     *
     * @param scraper 스크래퍼 객체
     * @param link 기사 URL
     * @param fallbackContent 스크래핑 실패 시 사용할 대체 콘텐츠
     * @return 스크래핑된 콘텐츠 또는 대체 콘텐츠
     */
    private String scrapeContent(ContentScraper scraper, String link, String fallbackContent) {
        try {
            String scrapedContent = scraper.scrapeContent(link);
            return scrapedContent != null && !scrapedContent.isEmpty()
                    ? scrapedContent
                    : fallbackContent;
        } catch (ArticleCollectorException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.INVALID_JOB_PARAMETER, e);
        } catch (Exception e) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.FEED_PARSING_ERROR, e);
        }
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
}