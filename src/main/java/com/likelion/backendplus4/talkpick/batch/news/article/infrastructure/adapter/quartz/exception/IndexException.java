package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.quartz.exception;

import com.likelion.backendplus4.talkpick.batch.common.exception.CustomException;
import com.likelion.backendplus4.talkpick.batch.common.exception.error.ErrorCode;

public class IndexException extends CustomException {
	private final ErrorCode errorCode;

	public IndexException(ErrorCode errorCode) {
		super(errorCode);
		this.errorCode = errorCode;
	}

	public IndexException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
		this.errorCode = errorCode;
	}

	@Override
	public ErrorCode getErrorCode() {
		return errorCode;
	}
}
