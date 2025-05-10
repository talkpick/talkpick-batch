package com.likelion.backendplus4.talkpick.batch.common.exception.error;

import lombok.AllArgsConstructor;
import lombok.Getter;


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
 * @modified 2025-05-09
 * @since 2025-05-09
 */
@AllArgsConstructor
@Getter
public enum GlobalErrorCode {

    ILLEGAL_ARGUMENT_CODE(14001),
    NOT_FOUND_CODE(140002),
    METHOD_ARGUMENT_NOT_VALID_CODE(300001),
    BIND_EXCEPTION_CODE(300002),
    INTERNAL_SERVER_ERROR_CODE(500000);

    private final int code;

}
