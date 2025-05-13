package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.implement;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.batch.RssSource;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.AbstractRssMapper;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.factory.ScraperFactory;
import com.rometools.rome.feed.synd.SyndEntry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 국민일보 RSS 매퍼 구현체
 *
 * @author 양병학
 * @since 2025-05-10 최초 작성
 * @modified 2025-05-15 템플릿 메서드 패턴 적용, 의존성 주입 방식 개선
 */
@Component
public class KmibRssMapper extends AbstractRssMapper {

    private static final Pattern ARCID_PATTERN = Pattern.compile("arcid=([0-9]+)");
    private static final Pattern IMG_SRC_PATTERN = Pattern.compile("""
    <img\\s+src=["']([^"']+)["']
    """.trim());

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

        // 패턴이 일치하지 않는 경우 예외 발생
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
        // 먼저 부모 클래스의 메서드로 시도 (media:content 태그가 있을 경우)
        String mediaContent = super.extractImageUrl(entry);
        if (!mediaContent.isEmpty()) {
            return mediaContent;
        }

        // description에서 이미지 URL 추출 시도
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
}