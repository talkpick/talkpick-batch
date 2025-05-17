package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.embedding.exception;

import com.likelion.backendplus4.talkpick.batch.common.exception.CustomException;
import com.likelion.backendplus4.talkpick.batch.common.exception.error.ErrorCode;

public class EmbeddingException extends CustomException {
	private final ErrorCode errorCode;

	public EmbeddingException(ErrorCode errorCode) {
		super(errorCode);
		this.errorCode = errorCode;
	}

	public EmbeddingException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
		this.errorCode = errorCode;
	}

	@Override
	public ErrorCode getErrorCode() {
		return errorCode;
	}
}
