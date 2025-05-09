package com.likelion.backendplus4.talkpick.batch.common.exception;

import com.likelion.backendplus4.talkpick.batch.common.exception.error.ErrorCode;

/**
 * 사용자 정의 예외의 추상 클래스 애플리케이션 전역에서 사용하는 공통 예외 상위 타입이다.
 *
 * @modified 2025-05-09
 * @since 2025-04-16
 */
public abstract class CustomException extends RuntimeException {


    // 메시지만 포함하는 기본 생성자
    public CustomException(ErrorCode errorCode) {
        super(errorCode.message());
    }

    // 메시지 + 원인 예외 포함하는 생상자
    public CustomException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.message(), cause);
    }

    public abstract ErrorCode getErrorCode();
}