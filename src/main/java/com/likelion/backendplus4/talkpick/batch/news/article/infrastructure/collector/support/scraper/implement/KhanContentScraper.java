package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.implement;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.ContentScraper;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.util.HtmlScraperUtils;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 경향신문 기사 본문 스크래퍼 구현체
 *
 * @author 양병학
 * @since 2025-05-13 최초 작성
 */
@Slf4j
@Component
public class KhanContentScraper implements ContentScraper {

    /**
     * 경향신문 기사 URL에서 본문 내용을 문단 단위로 스크래핑
     *
     * @param url 기사 URL
     * @return 문단 단위로 나눈 본문 리스트
     * @throws ArticleCollectorException 스크래핑 중 오류 발생 시
     */
    @Override
    public List<String> scrapeParagraphs(String url) throws ArticleCollectorException {
        Document document = connectToUrl(url);
        List<String> content = extractKhanContent(document);

        if (content == null || content.isEmpty() || content.stream().allMatch(String::isEmpty)) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.EMPTY_ARTICLE_CONTENT);
        }

        return content;
    }

    /**
     * 경향신문 본문 추출 (article.art_body에서 h3, div.art_photo 제외)
     *
     * @param document JSoup Document
     * @return 문단 리스트
     * @throws ArticleCollectorException 본문 파싱 중 오류 발생 시
     */
    private List<String> extractKhanContent(Document document) throws ArticleCollectorException {
        try {
            List<String> selectors = List.of(
                    "article.art_body",
                    "div.art_body",
                    "div.article_view",
                    "div.article-body"
            );
            // 가장 먼저 매칭되는 Element 하나만 찾기
            Element artBody = selectors.stream()
                    .map(sel -> HtmlScraperUtils.findElement(document, sel))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
            // 못 찾았으면 빈 리스트 반환
            if (artBody == null) {
                return new ArrayList<>();
            }
            // 찾았으면 실제 파싱 로직 호출
            return extractKhanContentFromElement(artBody);
        } catch (Exception e) {
            throw new ArticleCollectorException(
                    ArticleCollectorErrorCode.SCRAPER_PARSING_ERROR, e
            );
        }
    }

    /**
     * 경향신문 본문 요소에서 콘텐츠 추출
     *
     * @param artBody 기사 본문 요소
     * @return 문단 리스트
     * @throws ArticleCollectorException 본문 요소 처리 중 오류 발생 시
     */
    private List<String> extractKhanContentFromElement(Element artBody) throws ArticleCollectorException {
        try {
            Element processedBody = HtmlScraperUtils.removeTags(artBody, "h3", "div.art_photo", "img");

            processedBody.select("*").forEach(el -> {
                el.removeAttr("align");
                el.removeAttr("vspace");
                el.removeAttr("hspace");
                el.removeAttr("style");
                el.removeAttr("width");
                el.removeAttr("height");
            });

            Elements paragraphs = processedBody.select("p");

            if (paragraphs.isEmpty()) {
                paragraphs = processedBody.select("div.article_paragraph");
            }

            if (paragraphs.isEmpty()) {
                paragraphs = processedBody.select("span.article_text");
            }

            if (paragraphs.isEmpty()) {
                List<String> fallback = new ArrayList<>();
                String fullText = processedBody.text().trim();
                if (!fullText.isEmpty()) {
                    fallback.add(fullText);
                }
                return fallback;
            }

            List<String> result = new ArrayList<>();
            for (Element p : paragraphs) {
                String text = p.text().trim();
                if (!text.isEmpty()) {
                    result.add(text);
                }
            }

            return result;
        } catch (Exception e) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.SCRAPER_PARSING_ERROR, e);
        }
    }

    /**
     * 경향신문 기사 URL에서 본문 내용을 PARAGRAPH_BREAK로 구분된 문자열로 스크래핑
     *
     * @param url 기사 URL
     * @return PARAGRAPH_BREAK로 구분된 본문 문자열
     * @throws ArticleCollectorException 스크래핑 중 오류 발생 시
     */
    @Override
    public String scrapeContent(String url) throws ArticleCollectorException {
        Document document = connectToUrl(url);
        List<String> paragraphs = extractKhanContent(document);

        if (paragraphs == null || paragraphs.isEmpty() || paragraphs.stream().allMatch(String::isEmpty)) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.EMPTY_ARTICLE_CONTENT);
        }

        return String.join("PARAGRAPH_BREAK", paragraphs);
    }

    /**
     * 경향신문 기사 URL에서 이미지 URL을 스크래핑
     *
     * @param url 기사 URL
     * @return 스크래핑된 이미지 URL
     * @throws ArticleCollectorException 스크래핑 중 오류 발생 시
     */
    @Override
    public String scrapeImageUrl(String url) throws ArticleCollectorException {
        Document document = connectToUrl(url);
        String imageUrl = extractImageUrlFromDocument(document);

        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.EMPTY_ARTICLE_IMAGE);
        }

        return imageUrl;
    }

    /**
     * Document에서 이미지 URL 추출
     *
     * @param document 파싱된 JSoup Document
     * @return 추출된 이미지 URL
     * @throws ArticleCollectorException 이미지 추출 중 오류 발생 시
     */
    private String extractImageUrlFromDocument(Document document) throws ArticleCollectorException {
        try {
            Element metaImg = document.selectFirst("meta[property=og:image]");
            if (metaImg != null && !metaImg.attr("content").isEmpty()) {
                return metaImg.attr("content");
            }

            Element mainImg = document.selectFirst("picture img");
            if (mainImg != null && !mainImg.attr("src").isEmpty()) {
                return mainImg.attr("abs:src");
            }

            Element source = document.selectFirst("picture source");
            if (source != null && !source.attr("srcset").isEmpty()) {
                String srcset = source.attr("srcset");
                String[] sources = srcset.split(",");
                if (sources.length > 0) {
                    String firstSource = sources[0].trim().split("\\s+")[0];
                    return source.absUrl("srcset").isEmpty() ? firstSource : source.absUrl("srcset");
                }
            }

            Element contentImg = document.selectFirst("article.art_body img");
            if (contentImg != null && !contentImg.attr("src").isEmpty()) {
                return contentImg.attr("abs:src");
            }

            Element imgContainer = document.selectFirst("div.art_photo img");
            if (imgContainer != null && !imgContainer.attr("src").isEmpty()) {
                return imgContainer.attr("abs:src");
            }

            Element figureImg = document.selectFirst("figure img");
            if (figureImg != null && !figureImg.attr("src").isEmpty()) {
                return figureImg.attr("abs:src");
            }

            Element anyImg = document.selectFirst("img");
            if (anyImg != null && !anyImg.attr("src").isEmpty()) {
                return anyImg.attr("abs:src");
            }

            return "";
        } catch (Exception e) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.SCRAPER_PARSING_ERROR, e);
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