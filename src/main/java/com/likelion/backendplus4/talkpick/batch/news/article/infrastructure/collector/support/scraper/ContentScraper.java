package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    Logger logger = LoggerFactory.getLogger(ContentScraper.class);

    int MAX_RETRY_COUNT = 3;
    long RETRY_DELAY_MS = 1000;

    /**
     * 뉴스 URL에서 본문 내용을 문단 단위로 스크래핑
     *
     * @param url 뉴스 URL
     * @return 문단 단위로 나눈 본문 리스트
     * @throws ArticleCollectorException 스크래핑 중 오류 발생 시
     */
    List<String> scrapeParagraphs(String url) throws ArticleCollectorException;

    /**
     * 기사 URL에서 본문 내용을 PARAGRAPH_BREAK로 구분된 문자열로 스크래핑
     *
     * @param url 기사 URL
     * @return PARAGRAPH_BREAK로 구분된 본문 문자열
     */
    String scrapeContent(String url);

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
        return executeWithRetry(() -> {
            try {
                return Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .timeout(5000)
                        .ignoreContentType(true)
                        .maxBodySize(1024 * 1024)
                        .followRedirects(true)
                        .get();
            } catch (IOException e) {
                throw new ArticleCollectorException(ArticleCollectorErrorCode.FEED_CONNECTION_ERROR, e);
            }
        }, ArticleCollectorErrorCode.FEED_CONNECTION_ERROR);
    }

    /**
     * 재시도 기능이 있는 스크래핑 메서드 템플릿
     *
     * @param <T> 반환 타입
     * @param operation 실행할 작업
     * @param errorCode 예외 발생 시 사용할 에러 코드
     * @return 작업 결과
     * @throws ArticleCollectorException 최대 재시도 후에도 실패할 경우
     */
    default <T> T executeWithRetry(ScraperOperation<T> operation, ArticleCollectorErrorCode errorCode) {
        int retryCount = 0;
        Exception lastException = null;

        while (retryCount < MAX_RETRY_COUNT) {
            try {
                return operation.execute();
            } catch (ArticleCollectorException ace) {
                lastException = ace;
                if (retryCount == 0) {
                    logger.info("스크래핑 첫 시도 실패, 재시도 중: {}", ace.getMessage());
                } else {
                    logger.warn("스크래핑 재시도 {}/{} 실패", retryCount + 1, MAX_RETRY_COUNT);
                }
            } catch (Exception e) {
                lastException = e;
                if (retryCount == 0) {
                    logger.info("스크래핑 중 예외 발생, 재시도 중: {}", e.getMessage());
                } else {
                    logger.warn("스크래핑 재시도 {}/{} 중 예외 발생", retryCount + 1, MAX_RETRY_COUNT);
                }
            }

            retryCount++;

            if (retryCount < MAX_RETRY_COUNT) {
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new ArticleCollectorException(errorCode, ie);
                }
            }
        }

        logger.error("스크래핑 최대 재시도 횟수({}) 초과 후 실패", MAX_RETRY_COUNT);
        if (lastException instanceof ArticleCollectorException) {
            throw (ArticleCollectorException) lastException;
        } else {
            throw new ArticleCollectorException(errorCode, lastException);
        }
    }

    /**
     * 스크래퍼 작업 함수형 인터페이스
     *
     * @param <T> 반환 타입
     */
    @FunctionalInterface
    interface ScraperOperation<T> {
        T execute() throws Exception;
    }
}