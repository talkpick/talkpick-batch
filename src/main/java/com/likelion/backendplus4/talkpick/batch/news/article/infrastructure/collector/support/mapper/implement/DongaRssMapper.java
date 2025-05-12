package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.implement;

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
        return entry.getUri() != null ? entry.getUri() : source.getCodePrefix() + System.currentTimeMillis();
    }

    /**
     * 카테고리 정보 추출, 여러 카테고리를 쉼표로 구분하여 결합
     *
     * @param entry RSS 항목
     * @param source RSS 소스 정보
     * @return 결합된 카테고리 문자열
     */
    @Override
    protected String extractCategory(SyndEntry entry, RssSource source) {
        if (!entry.getCategories().isEmpty()) {
            return entry.getCategories().stream()
                    .map(SyndCategory::getName)
                    .collect(Collectors.joining(", "));
        }
        return source.getCategoryName();
    }
}