package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.implement;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.ContentScraper;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.util.HtmlScraperUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 경향신문 기사 본문 스크래퍼 구현체
 *
 * @author 양병학
 * @since 2025-05-13 최초 작성
 */
@Component
public class KhanContentScraper implements ContentScraper {

    /**
     * 경향신문 기사 URL에서 본문 내용을 문단 단위로 스크래핑
     *
     * @param url 기사 URL
     * @return 문단 단위로 나눈 본문 리스트
     */
    @Override
    public List<String> scrapeParagraphs(String url) {
        try {
            Document document = Jsoup.connect(url).get();
            return extractKhanContent(document);
        } catch (IOException e) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.FEED_PARSING_ERROR, e);
        }
    }

    /**
     * 경향신문 본문 추출 (article.art_body에서 h3, div.art_photo 제외)
     *
     * @param document JSoup Document
     * @return 문단 리스트
     */
    private List<String> extractKhanContent(Document document) {
        Element artBody = HtmlScraperUtils.findElement(document, "article.art_body");
        return null == artBody ? new ArrayList<>() : extractKhanContentFromElement(artBody);
    }

    /**
     * 경향신문 본문 요소에서 콘텐츠 추출
     *
     * @param artBody 기사 본문 요소
     * @return 문단 리스트
     */
    private List<String> extractKhanContentFromElement(Element artBody) {
        Element processedBody = HtmlScraperUtils.removeTags(artBody, "h3", "div.art_photo");

        Elements paragraphs = processedBody.select("p");

        return paragraphs.stream()
                .map(Element::text)
                .filter(text -> !text.trim().isEmpty())
                .toList();
    }

    /**
     * 경향신문 기사 URL에서 본문 내용을 텍스트로 스크래핑
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
     * 경향신문 기사 URL에서 이미지 URL을 스크래핑
     *
     * @param url 기사 URL
     * @return 스크래핑된 이미지 URL
     */
    @Override
    public String scrapeImageUrl(String url) {
        try {
            Document document = Jsoup.connect(url).get();
            return HtmlScraperUtils.extractImageUrl(document, "article.art_body div.art_photo img");
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * 지원하는 매퍼 타입 반환
     *
     * @return 경향신문 매퍼 타입 (kh)
     */
    @Override
    public String getSupportedMapperType() {
        return "kh";
    }
}