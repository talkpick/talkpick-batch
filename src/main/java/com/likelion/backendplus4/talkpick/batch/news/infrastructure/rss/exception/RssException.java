package com.likelion.backendplus4.talkpick.batch.news.infrastructure.rss.exception;

import lombok.Getter;

/**
 * RSS 예외처리 클래스
 *
 * @author 양병학
 * @since 2025-05-10
 */
@Getter
public class RssException extends RuntimeException {

    private final RssErrorCode errorCode;

    public RssException(RssErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 상세 메시지 생성자
     *
     * @param errorCode 오류 코드
     * @param message 상세 메시지
     */
    public RssException(RssErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 원인 예외 생성자
     *
     * @param errorCode 오류 코드
     * @param cause 원인 예외
     */
    public RssException(RssErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    /**
     * 상세 메시지및 원인 예외 생성자
     *
     * @param errorCode 오류 코드
     * @param message 상세 메시지
     * @param cause 원인 예외
     */
    public RssException(RssErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 오류 코드와 메시지를 포함한 문자열 반환
     */
    @Override
    public String toString() {
        return String.format("[%s] %s", errorCode.getCode(), getMessage());
    }
}