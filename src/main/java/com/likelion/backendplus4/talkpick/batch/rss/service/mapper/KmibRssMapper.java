package com.likelion.backendplus4.talkpick.batch.rss.service.mapper;

import com.likelion.backendplus4.talkpick.batch.rss.model.RssSource;
import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndEntry;
import com.likelion.backendplus4.talkpick.batch.rss.entity.RssNews;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class KmibRssMapper implements RssMapper {

    private static final Pattern ARCID_PATTERN = Pattern.compile("arcid=([0-9]+)");

    @Override
    public RssNews mapToRssNews(SyndEntry entry, RssSource source) {
        String arcId = extractArcIdFromLink(entry.getLink());
        String guid = source.getCodePrefix() + arcId;

        String description = "";
        if (entry.getDescription() != null) {
            description = entry.getDescription().getValue();
        }

        return RssNews.builder()
                .title(entry.getTitle())
                .link(entry.getLink())
                .pubDate(convertToLocalDateTime(entry.getPublishedDate()))
                .category(source.getCategoryName())  // Enum에서 직접 카테고리 이름 가져옴
                .guid(guid)
                .description(description)
                .isSummary(false)
                .build();
    }

    @Override
    public String getMapperType() {
        return "km";
    }

    private String extractArcIdFromLink(String link) {
        if (link == null) return "";

        Matcher matcher = ARCID_PATTERN.matcher(link);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return link;
    }

    private LocalDateTime convertToLocalDateTime(Date date) {
        return null != date
                ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                : LocalDateTime.now();
    }
}