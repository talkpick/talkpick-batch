package com.likelion.backendplus4.talkpick.batch.chat.exception.error;

import org.springframework.http.HttpStatus;

import com.likelion.backendplus4.talkpick.batch.common.exception.error.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatBatchErrorCode implements ErrorCode {
	STREAM_READ_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 460001, "Redis Stream 읽기 실패"),
	BATCH_LAUNCH_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 460002, "Batch 실행 실패");

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
