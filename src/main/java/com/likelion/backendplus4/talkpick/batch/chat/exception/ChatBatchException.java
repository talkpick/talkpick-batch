package com.likelion.backendplus4.talkpick.batch.chat.exception;

import com.likelion.backendplus4.talkpick.batch.common.exception.CustomException;
import com.likelion.backendplus4.talkpick.batch.common.exception.error.ErrorCode;

public class ChatBatchException extends CustomException {

	private final ErrorCode errorCode;

	@Override
	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public ChatBatchException(ErrorCode errorCode) {
		super(errorCode);
		this.errorCode = errorCode;
	}

	public ChatBatchException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
		this.errorCode = errorCode;
	}
}
