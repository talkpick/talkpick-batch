package com.likelion.backendplus4.talkpick.batch.sample.common.exception.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.likelion.backendplus4.talkpick.batch.sample.common.exception.application.in.ExceptionSampleUseCase;
import com.likelion.backendplus4.talkpick.batch.sample.common.exception.exception.SampleException;
import com.likelion.backendplus4.talkpick.batch.sample.common.exception.exception.error.ExceptionSampleErrorCode;
import org.springframework.stereotype.Service;

@Service
public class ExceptionSampleService implements ExceptionSampleUseCase {

    /**
     * 예외 처리 예시 로직입니다.
     */
    @Override
    public String failCase1() {
        boolean somethingWrong = true;
        if (somethingWrong) {
            throw new SampleException(ExceptionSampleErrorCode.SAMPLE_EXCEPTION);
        }
        return null;
    }

    /**
     * 예외 처리 예시 로직입니다.
     * e를 담아서 반환하는 경우 입니다.
     */
    @Override
    public String failCase2(String arrayNode) {
        try {
            throw new JsonProcessingException("강제 예외 발생") { };
        } catch (JsonProcessingException e) {
            // 원인 예외(e)를 함께 전달
            throw new SampleException(ExceptionSampleErrorCode.SAMPLE_EXCEPTION, e);
        }
    }


}
