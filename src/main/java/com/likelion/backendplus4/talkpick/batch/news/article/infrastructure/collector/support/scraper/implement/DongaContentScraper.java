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
import java.util.Objects;
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
        List<String> content = extractContent(url, document);
        validateContent(content);
        return content;
    }

    /**
     * URL과 문서에 따라 적절한 콘텐츠 추출 메서드를 호출
     *
     * @param url 기사 URL
     * @param document 파싱된 JSoup Document
     * @return 추출된 문단 리스트
     * @throws ArticleCollectorException 콘텐츠 추출 중 오류 발생 시
     */
    private List<String> extractContent(String url, Document document) throws ArticleCollectorException {
        if (isSportsArticle(url, document)) {
            return extractDongaSportsContent(document);
        }
        return extractDongaContent(document);
    }

    /**
     * 추출된 콘텐츠의 유효성 검증
     *
     * @param content 추출된 문단 리스트
     * @throws ArticleCollectorException 콘텐츠가 비어있거나 유효하지 않을 때
     */
    private void validateContent(List<String> content) throws ArticleCollectorException {
        if (content == null || content.isEmpty() || content.stream().allMatch(String::isEmpty)) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.EMPTY_ARTICLE_CONTENT);
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
        try {
            Element newsView = findNewsViewElement(document);
            if (null == newsView) {
                return new ArrayList<>();
            }

            // HTML 처리 및 불필요한 태그 제거
            Element processedView = processHtmlElement(newsView);

            // 전체 텍스트 추출
            String fullText = processedView.text();

            // 문단 추출
            List<String> paragraphs = extractParagraphsFromText(fullText);

            // 문단이 없는 경우 전체 텍스트를 하나의 문단으로 처리
            if (paragraphs.isEmpty() && !fullText.trim().isEmpty()) {
                paragraphs.add(fullText.trim());
            }

            return paragraphs;
        } catch (Exception e) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.SCRAPER_PARSING_ERROR, e);
        }
    }

    /**
     * 문서에서 뉴스 본문 영역 찾기
     *
     * @param document JSoup Document
     * @return 뉴스 본문 Element
     */
    private Element findNewsViewElement(Document document) {
        return HtmlScraperUtils.findElement(document, "section.news_view");
    }

    /**
     * HTML 요소 처리 - <br> 태그를 문단 구분자로 변환하고 불필요한 태그 제거
     *
     * @param element 처리할 HTML 요소
     * @return 처리된 HTML 요소
     */
    private Element processHtmlElement(Element element) {
        String html = element.html();
        html = html.replaceAll("<br\\s*/?\\s*>", "PARAGRAPH_BREAK");
        Element parsedElement = Jsoup.parse(html).body();

        return HtmlScraperUtils.removeTags(parsedElement, "h2", "figure", "img");
    }

    /**
     * 텍스트에서 문단 추출
     *
     * @param text 전체 텍스트
     * @return 문단 리스트
     */
    private List<String> extractParagraphsFromText(String text) {
        String[] paragraphsArray = text.split("PARAGRAPH_BREAK");

        return Arrays.stream(paragraphsArray)
                .map(String::trim)
                .filter(p -> !p.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 동아일보 스포츠 기사 본문 추출 (div.article_word#article_body)
     *
     * @param document JSoup Document
     * @return 문단 리스트
     * @throws ArticleCollectorException 본문 파싱 중 오류 발생 시
     */
    private List<String> extractDongaSportsContent(Document document) throws ArticleCollectorException {
        // 시도할 선택자들을 배열로 정의
        String[] selectors = {
                "div.article_word#article_body",
                "div.article_word"
        };

        Element articleBody = Arrays.stream(selectors)
                .map(selector -> HtmlScraperUtils.findElement(document, selector))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        if (articleBody == null) {
            return new ArrayList<>();
        }

        try {
            String[] selectorsToRemove = {
                    "div.photoAd",
                    "div.subcont_ad01",
                    "div.view_center",
                    "p.copyright"
            };

            Arrays.stream(selectorsToRemove)
                    .forEach(selector -> articleBody.select(selector).remove());

            String fullText = processHtmlAndExtractText(
                    articleBody, "img", "script", "style");

            return Arrays.stream(fullText.split("PARAGRAPH_BREAK"))
                    .map(String::trim)
                    .filter(p -> !p.isEmpty())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.SCRAPER_PARSING_ERROR, e);
        }
    }

    private String processHtmlAndExtractText(Element element, String... tagsToRemove) {
        String html = element.html();
        html = html.replaceAll("<br\\s*/?\\s*>", "PARAGRAPH_BREAK");
        Element parsedElement = Jsoup.parse(html).body();

        Element processedElement = HtmlScraperUtils.removeTags(parsedElement, tagsToRemove);

        return processedElement.text();
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