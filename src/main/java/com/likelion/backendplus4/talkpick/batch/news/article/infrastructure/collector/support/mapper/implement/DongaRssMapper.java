package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.implement;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.batch.RssSource;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.AbstractRssMapper;
import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndEntry;

import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * 동아일보 RSS 매퍼 구현체
 *
 * @author 양병학
 * @since 2025-05-10 최초 작성
 * @modified 2025-05-13 AbstractRssMapper 상속 구조로 변경 및 활성화
 */
@Component
public class DongaRssMapper extends AbstractRssMapper {

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
     */
    private String extractUniqueIdFromLink(String link) {
        if (link == null) {
            return String.valueOf(System.currentTimeMillis());
        }

        try {
            String[] parts = link.split("/");
            if (parts.length >= 2) {
                return parts[parts.length - 2];
            }
        } catch (Exception e) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.ITEM_MAPPING_ERROR);
        }

        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * 카테고리 enum 정보 추출
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
     * 이미지 URL 추출 메서드
     * 동아일보 RSS feed에서 media:content 태그에서 이미지 URL 추출
     *
     * @param entry RSS 항목
     * @return 이미지 URL
     */
    @Override
    protected String extractImageUrl(SyndEntry entry) {
        return entry.getForeignMarkup().stream()
                .filter(element -> "content".equals(element.getName()) &&
                        "media".equals(element.getNamespacePrefix()))
                .findFirst()
                .map(element -> element.getAttributeValue("url"))
                .orElse("");
    }
}