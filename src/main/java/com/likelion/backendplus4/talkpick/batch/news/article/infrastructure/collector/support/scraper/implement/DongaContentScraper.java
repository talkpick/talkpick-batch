package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.implement;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.ContentScraper;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.util.HtmlScraperUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 동아일보 기사 본문 스크래퍼 구현체
 *
 * @author 양병학
 * @since 2025-05-13 최초 작성
 * @modified 2025-05-17 동아일보 스포츠 기사 스크래핑 기능 추가
 */
@Component
public class DongaContentScraper implements ContentScraper {
    private static final Logger logger = LoggerFactory.getLogger(DongaContentScraper.class);

    /**
     * 동아일보 기사 URL에서 본문 내용, 문단 단위로 스크래핑
     *
     * @param url 기사 URL
     * @return 문단 단위로 문단 텍스트
     */
    @Override
    public List<String> scrapeParagraphs(String url) {
        return executeWithRetry(() -> {
            Document document = connectToUrl(url);
            List<String> content;

            if (isSportsArticle(url, document)) {
                content = extractDongaSportsContent(document);
            } else {
                content = extractDongaContent(document);
            }

            if (content == null || content.isEmpty() || content.stream().allMatch(String::isEmpty)) {
                logger.warn("스크래핑 결과가 비어있습니다: {}", url);
                throw new ArticleCollectorException(ArticleCollectorErrorCode.FEED_PARSING_ERROR);
            }

            return content;
        }, ArticleCollectorErrorCode.ITEM_MAPPING_ERROR);
    }

    /**
     * URL 또는 문서 구조를 기반으로 스포츠 기사인지 확인
     *
     * @param url 기사 URL
     * @param document 파싱된 JSoup Document
     * @return 스포츠 기사 여부
     */
    private boolean isSportsArticle(String url, Document document) {
        if (url.contains("sports.donga.com") || url.contains("/sports/")) {
            return true;
        }

        Element articleWord = document.selectFirst("div.article_word#article_body");
        return articleWord != null;
    }

    /**
     * 동아일보 일반 기사 본문 추출 (section.news_view에서 h2, figure 제외)
     *
     * @param document JSoup Document
     * @return 문단 리스트
     */
    private List<String> extractDongaContent(Document document) {
        Element newsView = HtmlScraperUtils.findElement(document, "section.news_view");
        if (null == newsView) {
            logger.warn("일반 기사 본문 요소(section.news_view)를 찾을 수 없습니다.");
            return new ArrayList<>();
        }

        String html = newsView.html();
        html = html.replaceAll("<br\\s*/?\\s*>", "PARAGRAPH_BREAK");
        newsView = Jsoup.parse(html).body();

        Element processedView = HtmlScraperUtils.removeTags(newsView, "h2", "figure", "img");
        String fullText = processedView.text();

        String[] paragraphsArray = fullText.split("PARAGRAPH_BREAK");

        List<String> paragraphs = Arrays.stream(paragraphsArray)
                .map(String::trim)
                .filter(p -> !p.isEmpty())
                .collect(Collectors.toList());

        if (paragraphs.isEmpty() && !fullText.trim().isEmpty()) {
            paragraphs.add(fullText.trim());
        }

        return paragraphs;
    }

    /**
     * 동아일보 스포츠 기사 본문 추출 (div.article_word#article_body)
     *
     * @param document JSoup Document
     * @return 문단 리스트
     */
    private List<String> extractDongaSportsContent(Document document) {
        Element articleBody = HtmlScraperUtils.findElement(document, "div.article_word#article_body");
        if (articleBody == null) {
            articleBody = HtmlScraperUtils.findElement(document, "div.article_word");
        }

        if (articleBody == null) {
            logger.warn("스포츠 기사 본문 요소(div.article_word)를 찾을 수 없습니다.");
            return new ArrayList<>();
        }

        articleBody.select("div.photoAd").remove();
        articleBody.select("div.subcont_ad01").remove();
        articleBody.select("div.view_center").remove();
        articleBody.select("p.copyright").remove();

        String html = articleBody.html();
        html = html.replaceAll("<br\\s*/?\\s*>", "PARAGRAPH_BREAK");
        articleBody = Jsoup.parse(html).body();

        Element processedBody = HtmlScraperUtils.removeTags(articleBody, "img", "script", "style");

        String fullText = processedBody.text();
        String[] paragraphsArray = fullText.split("PARAGRAPH_BREAK");

        List<String> paragraphs = Arrays.stream(paragraphsArray)
                .map(String::trim)
                .filter(p -> !p.isEmpty())
                .collect(Collectors.toList());

        if (paragraphs.isEmpty() && !fullText.trim().isEmpty()) {
            paragraphs.add(fullText.trim());
        }

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

        if (paragraphs.isEmpty() && !text.trim().isEmpty()) {
            paragraphs.add(text.trim());
        }

        return paragraphs;
    }

    /**
     * 동아일보 기사 URL에서 본문 내용을 PARAGRAPH_BREAK로 구분된 문자열로 스크래핑
     *
     * @param url 기사 URL
     * @return PARAGRAPH_BREAK로 구분된 본문 문자열
     */
    @Override
    public String scrapeContent(String url) {
        return executeWithRetry(() -> {
            List<String> paragraphs = scrapeParagraphs(url);
            return String.join("PARAGRAPH_BREAK", paragraphs);
        }, ArticleCollectorErrorCode.ITEM_MAPPING_ERROR);
    }

    /**
     * 동아일보 기사 URL에서 이미지 URL을 스크래핑
     *
     * @param url 기사 URL
     * @return 스크래핑된 이미지 URL
     */
    @Override
    public String scrapeImageUrl(String url) {
        return executeWithRetry(() -> {
            Document document = connectToUrl(url);

            if (isSportsArticle(url, document)) {
                return extractSportsImageUrlFromDocument(document);
            } else {
                return extractImageUrlFromDocument(document);
            }
        }, ArticleCollectorErrorCode.ITEM_MAPPING_ERROR);
    }

    /**
     * 일반 기사 Document에서 이미지 URL 추출
     *
     * @param document 파싱된 JSoup Document
     * @return 추출된 이미지 URL
     */
    private String extractImageUrlFromDocument(Document document) {
        return HtmlScraperUtils.extractImageUrl(document, "section.news_view figure img");
    }

    /**
     * 스포츠 기사 Document에서 이미지 URL 추출
     *
     * @param document 파싱된 JSoup Document
     * @return 추출된 이미지 URL
     */
    private String extractSportsImageUrlFromDocument(Document document) {
        Element ogImage = document.selectFirst("meta[property=og:image]");
        if (ogImage != null && !ogImage.attr("content").isEmpty()) {
            return ogImage.attr("content");
        }

        Element firstImg = document.selectFirst("div.article_word img");
        if (firstImg != null && !firstImg.attr("src").isEmpty()) {
            return firstImg.attr("abs:src");
        }

        Element anyImg = document.selectFirst("div.photo_view img");
        if (anyImg != null && !anyImg.attr("src").isEmpty()) {
            return anyImg.attr("abs:src");
        }

        return "";
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