package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.summary.batch.exception;

import com.likelion.backendplus4.talkpick.batch.common.exception.CustomException;
import com.likelion.backendplus4.talkpick.batch.common.exception.error.ErrorCode;

public class ArticleSummaryException extends CustomException {
	private final ErrorCode errorCode;

	public ArticleSummaryException(ErrorCode errorCode) {
		super(errorCode);
		this.errorCode = errorCode;
	}

	public ArticleSummaryException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
		this.errorCode = errorCode;
	}

	@Override
	public ErrorCode getErrorCode() {
		return errorCode;
	}
}
