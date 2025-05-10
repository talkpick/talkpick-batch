package com.likelion.backendplus4.talkpick.batch.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Slf4j
@Component
public class LogInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) {
        String traceId = generateTraceId();
        setTraceId(traceId);
        log.info("TraceId 생성 성공 - " + traceId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response, Object handler, Exception ex) {
        clearTraceId();
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString();
    }

    private void setTraceId(String traceId) {
        MDC.put("traceId", traceId);
    }

    private void clearTraceId() {
        MDC.clear();
    }
}
