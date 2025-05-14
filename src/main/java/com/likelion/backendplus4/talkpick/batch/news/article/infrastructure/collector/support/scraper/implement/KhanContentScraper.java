package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.implement;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.ContentScraper;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.util.HtmlScraperUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 경향신문 기사 본문 스크래퍼 구현체
 *
 * @author 양병학
 * @since 2025-05-13 최초 작성
 * @modified 2025-05-17 디버깅 및 로깅 추가
 */
@Component
public class KhanContentScraper implements ContentScraper {

    private static final Logger logger = LoggerFactory.getLogger(KhanContentScraper.class);

    /**
     * 경향신문 기사 URL에서 본문 내용을 문단 단위로 스크래핑
     *
     * @param url 기사 URL
     * @return 문단 단위로 나눈 본문 리스트
     */
    @Override
    public List<String> scrapeParagraphs(String url) {
        try {
            logger.info("경향신문 기사 스크래핑 시작: {}", url);
            Document document = connectToUrl(url);
            logger.info("경향신문 URL 연결 성공: {}", url);

            // 디버깅: HTML 구조 출력
            logger.debug("연결된 문서 제목: {}", document.title());

            List<String> paragraphs = extractKhanContent(document);
            logger.info("경향신문 본문 스크래핑 결과: 문단 수 = {}", paragraphs.size());

            if (paragraphs.isEmpty()) {
                logger.warn("경향신문 본문 스크래핑 실패: 문단을 찾을 수 없음");
            }

            return paragraphs;
        } catch (Exception e) {
            logger.error("경향신문 기사 스크래핑 오류: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 경향신문 본문 추출 (article.art_body에서 h3, div.art_photo 제외)
     *
     * @param document JSoup Document
     * @return 문단 리스트
     */
    private List<String> extractKhanContent(Document document) {
        // 다양한 컨테이너 선택자 시도
        Element artBody = HtmlScraperUtils.findElement(document, "article.art_body");

        if (artBody == null) {
            logger.debug("article.art_body 선택자로 본문을 찾을 수 없음, 다른 선택자 시도");
            // 다른 가능한 선택자 시도
            artBody = HtmlScraperUtils.findElement(document, "div.art_body");
        }

        if (artBody == null) {
            logger.debug("div.art_body 선택자로 본문을 찾을 수 없음, 다른 선택자 시도");
            artBody = HtmlScraperUtils.findElement(document, "div.article_view");
        }

        if (artBody == null) {
            logger.debug("div.article_view 선택자로 본문을 찾을 수 없음, 다른 선택자 시도");
            artBody = HtmlScraperUtils.findElement(document, "div.article-body");
        }

        if (artBody == null) {
            logger.warn("모든 선택자로 본문을 찾을 수 없음");

            // 전체 HTML 구조 로깅
            logger.debug("HTML 구조: {}", document.toString().substring(0, Math.min(1000, document.toString().length())));

            return new ArrayList<>();
        }

        logger.info("본문 컨테이너 발견: {}", artBody.tagName() + (artBody.hasAttr("class") ? "." + artBody.attr("class") : ""));
        return extractKhanContentFromElement(artBody);
    }

    /**
     * 경향신문 본문 요소에서 콘텐츠 추출
     *
     * @param artBody 기사 본문 요소
     * @return 문단 리스트
     */
    private List<String> extractKhanContentFromElement(Element artBody) {
        // h3, div.art_photo, img 태그 제거
        Element processedBody = HtmlScraperUtils.removeTags(artBody, "h3", "div.art_photo", "img");

        // 스타일 관련 속성 제거
        processedBody.select("*").forEach(el -> {
            el.removeAttr("align");
            el.removeAttr("vspace");
            el.removeAttr("hspace");
            el.removeAttr("style");
            el.removeAttr("width");
            el.removeAttr("height");
        });

        // p 태그 시도
        Elements paragraphs = processedBody.select("p");

        // p 태그가 없으면 다른 태그 시도
        if (paragraphs.isEmpty()) {
            logger.debug("p 태그가 없음, div 태그로 시도");
            paragraphs = processedBody.select("div.article_paragraph");
        }

        if (paragraphs.isEmpty()) {
            logger.debug("div.article_paragraph 태그가 없음, span 태그로 시도");
            paragraphs = processedBody.select("span.article_text");
        }

        if (paragraphs.isEmpty()) {
            logger.warn("모든 문단 선택자가 실패함, 본문 전체 텍스트를 하나의 문단으로 반환");
            List<String> fallback = new ArrayList<>();
            String fullText = processedBody.text().trim();
            if (!fullText.isEmpty()) {
                fallback.add(fullText);
            }
            return fallback;
        }

        List<String> result = paragraphs.stream()
                .map(Element::text)
                .filter(text -> !text.trim().isEmpty())
                .toList();

        logger.debug("추출된 문단 수: {}", result.size());
        return result;
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
        return String.join("\n\n", paragraphs);
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
            logger.info("경향신문 이미지 URL 스크래핑 시작: {}", url);
            Document document = connectToUrl(url);
            logger.info("경향신문 URL 연결 성공 (이미지 스크래핑): {}", url);

            String imageUrl = extractImageUrlFromDocument(document);

            if (imageUrl.isEmpty()) {
                logger.warn("경향신문 이미지 URL 스크래핑 실패: 이미지를 찾을 수 없음");
            } else {
                logger.info("경향신문 이미지 URL 스크래핑 성공: {}", imageUrl);
            }

            return imageUrl;
        } catch (Exception e) {
            logger.error("경향신문 이미지 URL 스크래핑 오류: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Document에서 이미지 URL 추출
     *
     * @param document 파싱된 JSoup Document
     * @return 추출된 이미지 URL
     */
    private String extractImageUrlFromDocument(Document document) {
        // 모든 시도를 로깅

        // 1. 메타 태그 시도 (og:image) - 가장 신뢰할 수 있는 소스
        Element metaImg = document.selectFirst("meta[property=og:image]");
        if (metaImg != null && !metaImg.attr("content").isEmpty()) {
            logger.debug("메타 태그에서 이미지 URL 찾음 (og:image)");
            return metaImg.attr("content");
        }

        // 2. 대표 이미지 (picture > img) 시도
        Element mainImg = document.selectFirst("picture img");
        if (mainImg != null && !mainImg.attr("src").isEmpty()) {
            logger.debug("picture > img에서 이미지 URL 찾음");
            return mainImg.attr("abs:src");
        }

        // 3. picture > source 시도
        Element source = document.selectFirst("picture source");
        if (source != null && !source.attr("srcset").isEmpty()) {
            logger.debug("picture > source에서 이미지 URL 찾음");
            String srcset = source.attr("srcset");
            String[] sources = srcset.split(",");
            if (sources.length > 0) {
                String firstSource = sources[0].trim().split("\\s+")[0];
                return source.absUrl("srcset").isEmpty() ? firstSource : source.absUrl("srcset");
            }
        }

        // 4. 본문 내 첫 번째 이미지 시도
        Element contentImg = document.selectFirst("article.art_body img");
        if (contentImg != null && !contentImg.attr("src").isEmpty()) {
            logger.debug("article.art_body img에서 이미지 URL 찾음");
            return contentImg.attr("abs:src");
        }

        // 5. div.art_photo 내 이미지 시도
        Element imgContainer = document.selectFirst("div.art_photo img");
        if (imgContainer != null && !imgContainer.attr("src").isEmpty()) {
            logger.debug("div.art_photo img에서 이미지 URL 찾음");
            return imgContainer.attr("abs:src");
        }

        // 6. figure 내 이미지 시도
        Element figureImg = document.selectFirst("figure img");
        if (figureImg != null && !figureImg.attr("src").isEmpty()) {
            logger.debug("figure img에서 이미지 URL 찾음");
            return figureImg.attr("abs:src");
        }

        // 7. 첫 번째 이미지 태그 시도 (최후의 수단)
        Element anyImg = document.selectFirst("img");
        if (anyImg != null && !anyImg.attr("src").isEmpty()) {
            logger.debug("첫 번째 img 태그에서 이미지 URL 찾음");
            return anyImg.attr("abs:src");
        }

        logger.warn("어떤 방법으로도 이미지 URL을 찾을 수 없음");
        return "";
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