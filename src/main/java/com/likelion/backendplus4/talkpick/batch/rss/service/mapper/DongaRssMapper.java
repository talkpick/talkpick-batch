package com.likelion.backendplus4.talkpick.batch.rss.service.mapper;

import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndEntry;
import com.likelion.backendplus4.talkpick.batch.rss.entity.RssNews;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class DongaRssMapper implements RssMapper {

    /*
        SyndEntry는 Rome 라이브러리에서 RSS 항목 나타내는 객체

        getTitle(): 제목 반환
        getLink(): 링크 반환
        getPublishedDate(): 발행일 반환
        getDescription(): 설명(요약) 반환
        getCategories(): 카테고리 목록 반환
        getUri(): 항목의 고유 식별자(제공사 고유번호 2자리 + guid) 반환
                  (예시: KM0028082827 [KM: 국민일보])
                  몇몇 피드들은 url에서 추출해서 사용

        getForeignMarkup(): RSS 2.0 기본 태그 외의 확장 태그(Dublin Core 등) 접근

        필요하면 객체 수정해서 사용, CustomEntry
     */

    @Override
    public RssNews mapToRssNews(SyndEntry entry) {
        // category
        String category = entry.getCategories().stream()
                .map(SyndCategory::getName)
                .collect(Collectors.joining(", "));

        return RssNews.builder()
                .title(entry.getTitle())
                .link(entry.getLink())
                .pubDate(convertToLocalDateTime(entry.getPublishedDate()))
                .category(category)
                .guid(entry.getUri()) // URI를 GUID로 사용
                .build();
    }

    private LocalDateTime convertToLocalDateTime(Date date) {
        return date != null
                ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                : LocalDateTime.now();
    }
}