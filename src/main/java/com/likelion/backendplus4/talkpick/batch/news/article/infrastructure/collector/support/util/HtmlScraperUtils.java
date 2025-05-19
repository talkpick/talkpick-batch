package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.util;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HTML 태그별 스크래핑 유틸리티 클래스
 *
 * @author 양병학
 * @since 2025-05-13 최초 작성
 */
public class HtmlScraperUtils {

    /**
     * 지정된 CSS 선택자에 해당하는 요소 찾기
     *
     * @param document JSoup Document
     * @param selector CSS 선택자
     * @return 찾은 요소, 없으면 null
     */
    public static Element findElement(Document document, String selector) {
        return null != document ? document.selectFirst(selector) : null;
    }

    /**
     * 지정된 CSS 선택자에 해당하는 모든 요소 찾기
     *
     * @param document JSoup Document
     * @param selector CSS 선택자
     * @return 찾은 요소들의 목록
     */
    public static Elements findElements(Document document, String selector) {
        return null != document ? document.select(selector) : new Elements();
    }

    /**
     * 특정 요소에서 지정된 태그들 제거
     *
     * @param element 처리할 요소
     * @param tagsToRemove 제거할 태그 목록 (예: "h2", "figure")
     * @return 태그가 제거된 요소 (원본은 변경되지 않음)
     */
    public static Element removeTags(Element element, String... tagsToRemove) {
        return null == element ? null : doRemoveTags(element, tagsToRemove);
    }

    private static Element doRemoveTags(Element element, String... tagsToRemove) {
        Element clone = element.clone();

        for (String tag : tagsToRemove) {
            clone.select(tag).remove();
        }

        return clone;
    }

    /**
     * 요소에서 텍스트 추출 (HTML 태그 제거)
     *
     * @param element 추출할 요소
     * @return 추출된 텍스트, 요소가 null이면 빈 문자열
     */
    public static String extractText(Element element) {
        return null != element ? element.text() : "";
    }

    /**
     * 여러 요소에서 텍스트 추출하여 결합
     *
     * @param elements 처리할 요소들
     * @param separator 텍스트 사이에 넣을 구분자 (예: "\n\n")
     * @return 결합된 텍스트
     */
    public static String extractCombinedText(Elements elements, String separator) {
        return null != elements && !elements.isEmpty()
                ? String.join(separator, elements.stream().map(Element::text).collect(Collectors.toList()))
                : "";
    }

    /**
     * p 태그의 내용을 개별 문단으로 추출
     *
     * @param container p 태그를 포함하는 요소
     * @return 각 p 태그의 내용을 담은 문단 리스트
     */
    public static List<String> extractParagraphs(Element container) {
        return null == container ? new ArrayList<>() : doParagraphExtraction(container);
    }

    /**
     * p 태그 추출 실제 로직
     *
     * @param container p 태그를 포함하는 요소
     * @return 추출된 문단 리스트
     */
    private static List<String> doParagraphExtraction(Element container) {
        Elements paragraphs = container.select("p");
        return paragraphs.stream()
                .map(Element::text)
                .filter(text -> !text.trim().isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * p 태그의 텍스트를 결합하여 추출
     *
     * @param container p 태그를 포함하는 요소
     * @param separator 텍스트 사이에 넣을 구분자 (기본값: "\n\n")
     * @return 결합된.텍스트
     */
    public static String extractParagraphText(Element container, String separator) {
        return null != container
                ? extractCombinedText(container.select("p"), separator)
                : "";
    }

    /**
     * p 태그의 텍스트를 줄바꿈으로 결합하여 추출 (기본 구분자: "\n\n")
     *
     * @param container p 태그를 포함하는 요소
     * @return 결합된 텍스트
     */
    public static String extractParagraphText(Element container) {
        return extractParagraphText(container, "\n\n");
    }

    /**
     * 이미지 URL 추출
     *
     * @param document JSoup Document
     * @param imgSelector 이미지 선택자
     * @return 이미지 URL, 없으면 빈 문자열
     */
    public static String extractImageUrl(Document document, String imgSelector) {
        Element img = findElement(document, imgSelector);
        return null != img ? img.absUrl("src") : "";
    }

    /**
     * HTML 요소 내용 처리 - 공통 메서드
     *
     * @param document JSoup Document
     * @param selector 요소 선택자
     * @param excludeTags 제외할 태그 목록
     * @return 처리된 텍스트
     */
    public static String processElement(Document document, String selector, String... excludeTags) {
        Element element = findElement(document, selector);
        return null != element
                ? extractText(removeTags(element, excludeTags))
                : "";
    }

    /**
     * section 태그 내용 처리
     *
     * @param document JSoup Document
     * @param sectionSelector section 태그 선택자
     * @param excludeTags 제외할 태그 목록
     * @return 처리된 텍스트
     */
    public static String processSection(Document document, String sectionSelector, String... excludeTags) {
        Element section = findElement(document, sectionSelector);
        return null != section
                ? extractText(removeTags(section, excludeTags))
                : "";
    }

    /**
     * article 태그 내용 처리
     *
     * @param document JSoup Document
     * @param articleSelector article 태그 선택자
     * @param excludeTags 제외할 태그 목록
     * @return 처리된 텍스트
     */
    public static String processArticle(Document document, String articleSelector, String... excludeTags) {
        Element article = findElement(document, articleSelector);
        return null != article
                ? extractText(removeTags(article, excludeTags))
                : "";
    }

    /**
     * div 태그 내용 처리
     *
     * @param document JSoup Document
     * @param divSelector div 태그 선택자
     * @param excludeTags 제외할 태그 목록
     * @return 처리된 텍스트
     */
    public static String processDiv(Document document, String divSelector, String... excludeTags) {
        Element div = findElement(document, divSelector);
        return null != div
                ? extractText(removeTags(div, excludeTags))
                : "";
    }
}