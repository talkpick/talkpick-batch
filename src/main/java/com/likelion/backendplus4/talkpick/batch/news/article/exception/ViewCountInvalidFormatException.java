package com.likelion.backendplus4.talkpick.batch.news.article.exception;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;

/**
 * 조회수 데이터의 형식이 잘못되었을 때 발생하는 예외 클래스입니다.
 *
 * @since 2025-05-21
 */
public class ViewCountInvalidFormatException extends ArticleCollectorException {

	/**
	 * 기본 에러 메시지로 예외를 생성합니다.
	 */
	public ViewCountInvalidFormatException() {
		super(ArticleCollectorErrorCode.VIEW_COUNT_INVALID_FORMAT);
	}

	/**
	 * 상세 메시지와 함께 예외를 생성합니다.
	 *
	 * @param message 상세 오류 메시지
	 */
	public ViewCountInvalidFormatException(String message) {
		super(ArticleCollectorErrorCode.VIEW_COUNT_INVALID_FORMAT);
	}

	/**
	 * 원인 예외와 함께 예외를 생성합니다.
	 *
	 * @param cause 원인 예외
	 */
	public ViewCountInvalidFormatException(Throwable cause) {
		super(ArticleCollectorErrorCode.VIEW_COUNT_INVALID_FORMAT, cause);
	}

	/**
	 * 상세 메시지와 원인 예외와 함께 예외를 생성합니다.
	 *
	 * @param message 상세 오류 메시지
	 * @param cause 원인 예외
	 */
	public ViewCountInvalidFormatException(String message, Throwable cause) {
		super(ArticleCollectorErrorCode.VIEW_COUNT_INVALID_FORMAT, cause);
	}
}