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
public class KhanRssMapper implements RssMapper {

    @Override
    public RssNews mapToRssNews(SyndEntry entry) {
        // 경향신문 dc:date를 사용
        LocalDateTime pubDate;
        if (entry.getPublishedDate() != null) {
            pubDate = convertToLocalDateTime(entry.getPublishedDate());
        } else {
            pubDate = entry.getForeignMarkup().stream()
                    .filter(element -> "date".equals(element.getName()) &&
                            "dc".equals(element.getNamespacePrefix()))
                    .findFirst()
                    .map(element -> {
                        try {
                            return LocalDateTime.parse(element.getValue());
                        } catch (Exception e) {
                            return LocalDateTime.now();
                        }
                    })
                    .orElse(LocalDateTime.now());
        }

        String category = entry.getCategories().stream()
                .map(SyndCategory::getName)
                .collect(Collectors.joining(", "));

        return RssNews.builder()
                .title(entry.getTitle())
                .link(entry.getLink())
                .pubDate(pubDate)
                .category(category)
                .guid(entry.getUri())
                .build();
    }

    private LocalDateTime convertToLocalDateTime(Date date) {
        return date != null
                ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                : LocalDateTime.now();
    }
}