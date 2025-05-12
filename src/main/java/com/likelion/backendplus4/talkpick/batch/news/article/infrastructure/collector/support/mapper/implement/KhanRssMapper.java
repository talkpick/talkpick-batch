package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.implement;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.batch.RssSource;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.AbstractRssMapper;
import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndEntry;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * 경향신문 RSS 매퍼 구현체
 *
 * @author 양병학
 * @since 2025-05-10 최초 작성
 * @modified 2025-05-13 AbstractRssMapper 상속 구조로 변경 및 활성화
 */
@Component
public class KhanRssMapper extends AbstractRssMapper {

    /**
     * 매퍼 타입 반환
     *
     * @return 매퍼 타입 (kh)
     */
    @Override
    public String getMapperType() {
        return "kh";
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
     * 발행일 추출, 경향신문은 dc:date 태그도 확인
     *
     * @param entry RSS 항목
     * @return 발행일 LocalDateTime
     */
    @Override
    protected LocalDateTime extractPubDate(SyndEntry entry) {
        if (entry.getPublishedDate() != null) {
            return convertToLocalDateTime(entry.getPublishedDate());
        }

        return extractDcDate(entry);
    }

    /**
     * Dublin Core date 태그에서 발행일 추출
     *
     * @param entry RSS 항목
     * @return 추출된 발행일, 없으면 현재 시간
     */
    private LocalDateTime extractDcDate(SyndEntry entry) {
        return entry.getForeignMarkup().stream()
                .filter(element -> "date".equals(element.getName()) &&
                        "dc".equals(element.getNamespacePrefix()))
                .findFirst()
                .map(element -> parseDateTime(element.getValue()))
                .orElse(LocalDateTime.now());
    }

    /**
     * 문자열을 LocalDateTime으로 파싱
     *
     * @param dateString 날짜 문자열
     * @return 파싱된 LocalDateTime, 실패 시 현재 시간
     */
    private LocalDateTime parseDateTime(String dateString) {
        try {
            return LocalDateTime.parse(dateString);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
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