package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HTML 파싱 및 문단 추출을 처리하는 유틸리티 클래스
 *
 * @author 양병학
 * @since 2025-05-18
 */
@Component
public class HtmlParser {

    /**
     * HTML 문자열에서 모든 태그를 제거하고 문단을 추출
     *
     * @param html HTML 문자열
     * @return 정제된 문단 리스트
     */
    public List<String> extractCleanParagraphs(String html) {
        if (html == null || html.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            String withBreaks = html.replaceAll("<br\\s*/?>", "PARAGRAPH_BREAK");
            String noTags = withBreaks.replaceAll("<[^>]*>", "");
            String decoded = decodeHtmlEntities(noTags);

            decoded = decoded.replaceAll("\\s+", " ").trim();
            String[] paragraphs = decoded.split("PARAGRAPH_BREAK");

            return Arrays.stream(paragraphs)
                    .map(String::trim)
                    .filter(p -> !p.isEmpty())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            List<String> fallback = new ArrayList<>();
            fallback.add(removeAllHtmlTags(html));
            return fallback;
        }
    }

    private String removeAllHtmlTags(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }

        String noTags = html.replaceAll("<[^>]*>", "");
        return decodeHtmlEntities(noTags).replaceAll("\\s+", " ").trim();
    }

    private String decodeHtmlEntities(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        return text.replace("&nbsp;", " ")
                .replace("&#160;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'");
    }
}
