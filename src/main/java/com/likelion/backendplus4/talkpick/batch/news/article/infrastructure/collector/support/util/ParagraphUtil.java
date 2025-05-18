package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.util;

import java.util.List;

/**
 * 문단 처리 유틸리티 클래스
 *
 * @author 양병학
 * @since 2025-05-18
 */
public class ParagraphUtil {

    /**
     * 문단 리스트를 구분자로 연결된 문자열로 직렬화
     *
     * @param paragraphs 문단 리스트
     * @return 직렬화된 문자열
     */
    public String serializeParagraphs(List<String> paragraphs) {
        if (paragraphs == null || paragraphs.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paragraphs.size(); i++) {
            if (i > 0) {
                sb.append("PARAGRAPH_BREAK");
            }
            sb.append(paragraphs.get(i));
        }

        return sb.toString();
    }
}