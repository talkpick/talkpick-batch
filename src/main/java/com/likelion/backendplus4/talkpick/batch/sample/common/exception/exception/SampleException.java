package com.likelion.backendplus4.talkpick.batch.sample.common.exception.exception;

import com.likelion.backendplus4.talkpick.batch.common.exception.CustomException;
import com.likelion.backendplus4.talkpick.batch.common.exception.error.ErrorCode;

public class SampleException extends CustomException {

    private final ErrorCode errorCode;

    public SampleException(ErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }

    public SampleException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
        this.errorCode = errorCode;
    }

    @Override
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
