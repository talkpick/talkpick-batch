package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;

/**
 * 뉴스 본문 스크랩 interface
 * 스크래핑 로직이 신문사 마다 다름
 *
 * @author 양병학
 * @since 2025-05-13 최초 작성
 */
public interface ContentScraper {

    /**
     * 뉴스 URL에서 본문 내용을 문단 단위로 스크래핑
     *
     * @param url 뉴스 URL
     * @return 문단 단위로 나눈 본문 리스트
     * @throws ArticleCollectorException 스크래핑 중 오류 발생 시
     */
    List<String> scrapeParagraphs(String url) throws ArticleCollectorException;

    /**
     * 뉴스 URL에서 본문 내용을 텍스트로 스크래핑
     *
     * @param url 뉴스 URL
     * @return 스크래핑된 본문
     * @throws ArticleCollectorException 스크래핑 중 오류 발생 시
     */
    String scrapeContent(String url) throws ArticleCollectorException;

    /**
     * 뉴스 URL에서 이미지 URL을 스크래핑
     *
     * @param url 뉴스 URL
     * @return 스크래핑된 이미지 URL
     */
    String scrapeImageUrl(String url);

    /**
     * 스크래퍼가 지원하는 Mapper type 반환
     *
     * @return Mapper Type 영문 2자 (예: "km", "da")
     */
    String getSupportedMapperType();


    /**
     * URL에 연결하여 Document 객체 반환 (기본 구현)
     *
     * @param url 연결할 URL
     * @return 파싱된 JSoup Document
     * @throws ArticleCollectorException 연결 오류 발생 시 FEED_PARSING_ERROR 예외 발생
     */
    default Document connectToUrl(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(5000)
                    .ignoreContentType(true)
                    .maxBodySize(1024 * 1024)
                    .followRedirects(true)
                    .get();
        } catch (IOException e) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.FEED_PARSING_ERROR, e);
        }
    }
}