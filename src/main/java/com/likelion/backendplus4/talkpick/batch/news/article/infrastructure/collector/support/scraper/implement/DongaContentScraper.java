package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.implement;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.ContentScraper;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.util.HtmlScraperUtils;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

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
@Slf4j
@Component
public class DongaContentScraper implements ContentScraper {

    /**
     * 동아일보 기사 URL에서 본문 내용, 문단 단위로 스크래핑
     *
     * @param url 기사 URL
     * @return 문단 단위로 문단 텍스트
     * @throws ArticleCollectorException 스크래핑 중 오류 발생 시
     */
    @Override
    public List<String> scrapeParagraphs(String url) throws ArticleCollectorException {
        Document document = connectToUrl(url);
        List<String> content;

        try {
            if (isSportsArticle(url, document)) {
                content = extractDongaSportsContent(document);
            } else {
                content = extractDongaContent(document);
            }

            if (content == null || content.isEmpty() || content.stream().allMatch(String::isEmpty)) {
                throw new ArticleCollectorException(ArticleCollectorErrorCode.EMPTY_ARTICLE_CONTENT);
            }

            return content;
        } catch (ArticleCollectorException e) {
            throw e;
        } catch (Exception e) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.SCRAPER_PARSING_ERROR, e);
        }
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
     * @throws ArticleCollectorException 본문 파싱 중 오류 발생 시
     */
    private List<String> extractDongaContent(Document document) throws ArticleCollectorException {
        Element newsView = HtmlScraperUtils.findElement(document, "section.news_view");
        if (null == newsView) {
            return new ArrayList<>();
        }

        try {
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
        } catch (Exception e) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.SCRAPER_PARSING_ERROR, e);
        }
    }

    /**
     * 동아일보 스포츠 기사 본문 추출 (div.article_word#article_body)
     *
     * @param document JSoup Document
     * @return 문단 리스트
     * @throws ArticleCollectorException 본문 파싱 중 오류 발생 시
     */
    private List<String> extractDongaSportsContent(Document document) throws ArticleCollectorException {
        Element articleBody = HtmlScraperUtils.findElement(document, "div.article_word#article_body");
        if (articleBody == null) {
            articleBody = HtmlScraperUtils.findElement(document, "div.article_word");
        }

        if (articleBody == null) {
            return new ArrayList<>();
        }

        try {
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
        } catch (Exception e) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.SCRAPER_PARSING_ERROR, e);
        }
    }

    /**
     * 동아일보 기사 URL에서 본문 내용을 PARAGRAPH_BREAK로 구분된 문자열로 스크래핑
     *
     * @param url 기사 URL
     * @return PARAGRAPH_BREAK로 구분된 본문 문자열
     * @throws ArticleCollectorException 스크래핑 중 오류 발생 시
     */
    @Override
    public String scrapeContent(String url) throws ArticleCollectorException {
        List<String> paragraphs = scrapeParagraphs(url);
        return String.join("PARAGRAPH_BREAK", paragraphs);
    }

    /**
     * 동아일보 RSS에서 이미지를 가져오므로 빈 문자열 반환 (구현 필요없음)
     *
     * @param url 기사 URL
     * @return 빈 문자열
     * @throws ArticleCollectorException 사용되지 않음
     */
    @Override
    public String scrapeImageUrl(String url) throws ArticleCollectorException {
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