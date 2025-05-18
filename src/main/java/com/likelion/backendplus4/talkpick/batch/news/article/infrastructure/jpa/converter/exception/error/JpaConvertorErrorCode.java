package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.converter.exception.error;

import org.springframework.http.HttpStatus;

import com.likelion.backendplus4.talkpick.batch.common.exception.error.ErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 에러 코드 인터페이스 각 에러 항목에 대한 HTTP 상태, 에러 번호, 메시지를 제공한다.
 * A[BB][CCC]
 * A (1자리) : 에러 심각도 (1~5)
 * 1: 클라이언트 오류
 * 2: 인증 관련 오류
 * 3: 사용자 관련 오류
 * 4: 서버 오류
 * 5: 시스템 오류
 *
 * BB (2자리) : 도메인 코드
 * 10: 사용자 관련 (ex: USER_NOT_FOUND)
 * 20: 인증 관련 (ex: AUTHORIZATION_FAILED)
 * 30: DB 관련 오류 (ex: DB_CONNECTION_FAILED)
 * 40: API 관련 오류 (ex: API_TIMEOUT)
 * 50: 시스템 오류 (ex: INTERNAL_SERVER_ERROR)
 *
 * CCC (3자리) : 세부 오류 순번
 * 001: 첫 번째 오류
 * 002: 두 번째 오류
 * 003: 세 번째 오류, 등등
 *
 * @since 2025-05-17
 */
@RequiredArgsConstructor
public enum JpaConvertorErrorCode implements ErrorCode {
	JSON_CONVERT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 440003, "JSON 컨버터 타입 변환 실패");

	private final HttpStatus status;
	private final int code;
	private final String message;

	@Override
	public HttpStatus httpStatus() {
		return status;
	}

	@Override
	public int codeNumber() {
		return code;
	}

	@Override
	public String message() {
		return message;
	}
}
