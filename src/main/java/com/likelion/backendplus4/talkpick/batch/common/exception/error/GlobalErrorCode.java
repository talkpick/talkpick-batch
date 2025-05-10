package com.likelion.backendplus4.talkpick.batch.common.exception.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum GlobalErrorCode {

    ILLEGAL_ARGUMENT_CODE(14001),
    NOT_FOUND_CODE(140002),
    METHOD_ARGUMENT_NOT_VALID_CODE(300001),
    BIND_EXCEPTION_CODE(300002),
    INTERNAL_SERVER_ERROR_CODE(500000);

    private final int code;

}
