package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.implement;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.batch.RssSource;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.AbstractRssMapper;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;
import com.rometools.rome.feed.synd.SyndEntry;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 국민일보 RSS 매퍼 구현체
 *
 * @author 양병학
 * @since 2025-05-10 최초 작성
 * @modified 2025-05-13 AbstractRssMapper 상속 구조로 변경
 */
@Component
public class KmibRssMapper extends AbstractRssMapper {

    private static final Pattern ARCID_PATTERN = Pattern.compile("arcid=([0-9]+)");

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
        String arcId = extractArcIdFromLink(entry.getLink());
        return source.getCodePrefix() + arcId;
    }

    /**
     * Rss 피드가 요약본인지 여부를 반환
     * 국민일보는 RSS에 전체 내용이 포함, false 반환
     *
     * @return 요약본 여부 (false: 전체 본문 제공)
     */
    @Override
    protected boolean getIsSummary() {
        return false;
    }

    /**
     * 링크에서 arcid 값 추출
     *
     * @param link 기사 링크
     * @return 추출된 arcid, 없으면 링크 그대로 반환
     */
    private String extractArcIdFromLink(String link) {
        if (link == null) {
            return "";
        }

        Matcher matcher = ARCID_PATTERN.matcher(link);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return link;
    }
}