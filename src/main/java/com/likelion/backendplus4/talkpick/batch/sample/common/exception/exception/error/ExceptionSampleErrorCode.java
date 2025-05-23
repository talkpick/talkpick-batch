package com.likelion.backendplus4.talkpick.batch.sample.common.exception.exception.error;

import com.likelion.backendplus4.talkpick.batch.common.exception.error.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum ExceptionSampleErrorCode implements ErrorCode {

    SAMPLE_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, 440000, "실패");

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
