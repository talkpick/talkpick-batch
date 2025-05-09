package com.likelion.backendplus4.talkpick.batch.temp.exception;

import com.likelion.backendplus4.talkpick.batch.common.exception.CustomException;
import com.likelion.backendplus4.talkpick.batch.common.exception.error.ErrorCode;

public class TempException extends CustomException {

    private final ErrorCode errorCode;

    public TempException(ErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }

    public TempException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
        this.errorCode = errorCode;
    }

    @Override
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
