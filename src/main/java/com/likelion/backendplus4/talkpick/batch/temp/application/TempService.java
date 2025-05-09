package com.likelion.backendplus4.talkpick.batch.temp.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.likelion.backendplus4.talkpick.batch.temp.exception.TempException;
import com.likelion.backendplus4.talkpick.batch.temp.exception.error.TempErrorCode;
import org.springframework.stereotype.Service;

@Service
public class TempService {

    /**
     * 예외 처리 예시 로직입니다.
     */
    public String failCase1() {
        boolean somethingWrong = true;
        if (somethingWrong) {
            throw new TempException(TempErrorCode.TEMP_EXCEPTION);
        }
        return null;
    }

    /**
     * 예외 처리 예시 로직입니다.
     * e를 담아서 반환하는 경우 입니다.
     */
    public String failCase2(ArrayNode arrayNode) {
        try {
            throw new JsonProcessingException("강제 예외 발생") { };
        } catch (JsonProcessingException e) {
            // 원인 예외(e)를 함께 전달
            throw new TempException(TempErrorCode.TEMP_EXCEPTION, e);
        }
    }


}
