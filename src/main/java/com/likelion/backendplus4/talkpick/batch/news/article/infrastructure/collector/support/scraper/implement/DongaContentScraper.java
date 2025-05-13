package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.implement;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.ContentScraper;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.util.HtmlScraperUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 동아일보 기사 본문 스크래퍼 구현체
 *
 * @author 양병학
 * @since 2025-05-13 최초 작성
 */
@Component
public class DongaContentScraper implements ContentScraper {

    /**
     * 동아일보 기사 URL에서 본문 내용, 문단 단위로 스크래핑
     *
     * @param url 기사 URL
     * @return 문단 단위로 문단 텍스트
     */
    @Override
    public List<String> scrapeParagraphs(String url) {
        try {
            Document document = Jsoup.connect(url).get();
            return extractDongaContent(document);
        } catch (IOException e) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.FEED_PARSING_ERROR, e);
        }
    }

    /**
     * 동아일보 본문 추출 (section.news_view에서 h2, figure 제외)
     *
     * @param document JSoup Document
     * @return 문단 리스트
     */
    private List<String> extractDongaContent(Document document) {
        Element newsView = HtmlScraperUtils.findElement(document, "section.news_view");
        if (null == newsView) {
            return new ArrayList<>();
        }

        Element processedView = HtmlScraperUtils.removeTags(newsView, "h2", "figure");

        String fullText = processedView.text();
        List<String> paragraphs = extractParagraphsByQuotes(fullText);

        return paragraphs;
    }

    /**
     * 큰따옴표 기준으로 문단 추출
     *
     * @param text 전체 텍스트
     * @return 문단 리스트
     */
    private List<String> extractParagraphsByQuotes(String text) {
        List<String> paragraphs = new ArrayList<>();

        String[] parts = text.split("\"");

        for (int i = 1; i < parts.length; i += 2) {
            String paragraph = parts[i].trim();
            if (!paragraph.isEmpty()) {
                paragraphs.add(paragraph);
            }
        }

        // 따옴표가 없거나 문단이 추출되지 않은 경우 전체 텍스트를 하나의 문단으로 처리
        if (paragraphs.isEmpty() && !text.trim().isEmpty()) {
            paragraphs.add(text.trim());
        }

        return paragraphs;
    }

    /**
     * 동아일보 기사 URL에서 본문 내용을 텍스트로 스크래핑
     *
     * @param url 기사 URL
     * @return 스크래핑된 본문
     */
    @Override
    public String scrapeContent(String url) {
        List<String> paragraphs = scrapeParagraphs(url);
        return paragraphs.stream().collect(Collectors.joining("\n\n"));
    }

    /**
     * 동아일보 기사 URL에서 이미지 URL을 스크래핑
     *
     * @param url 기사 URL
     * @return 스크래핑된 이미지 URL
     */
    @Override
    public String scrapeImageUrl(String url) {
        try {
            Document document = Jsoup.connect(url).get();
            return HtmlScraperUtils.extractImageUrl(document, "section.news_view figure img");
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * 지원하는 매퍼 타입 반환
     *
     * @return 동아일보 매퍼 타입 (da)
     */
    @Override
    public String getSupportedMapperType() {
        return "da";
    }
}