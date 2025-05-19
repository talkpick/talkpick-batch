package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.converter.exception;

import com.likelion.backendplus4.talkpick.batch.common.exception.CustomException;
import com.likelion.backendplus4.talkpick.batch.common.exception.error.ErrorCode;

/**
 * JPA AttributeConverter에서 변환 중 발생한 예외를 나타내는 커스텀 예외 클래스.
 *
 * <p>일반적으로 JSON 직렬화/역직렬화 중 오류가 발생했을 때 사용되며,
 * {@link CustomException}을 상속하고 {@link ErrorCode}를 통해 상세 오류 정보를 제공한다.
 *
 * @since 2025-05-17
 */
public class JpaConvertorException extends CustomException {
	private final ErrorCode errorCode;

	public JpaConvertorException(ErrorCode errorCode) {
		super(errorCode);
		this.errorCode = errorCode;
	}

	public JpaConvertorException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
		this.errorCode = errorCode;
	}

	@Override
	public ErrorCode getErrorCode() {
		return errorCode;
	}
}
