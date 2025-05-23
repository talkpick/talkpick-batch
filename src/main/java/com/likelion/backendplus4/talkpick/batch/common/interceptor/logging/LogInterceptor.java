package com.likelion.backendplus4.talkpick.batch.common.interceptor.logging;

import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 로깅을 위한 인터셉터 클래스
 * HTTP 요청 전후로 TraceId를 생성·설정·제거하여 로그 추적 정보를 지원한다.
 *
 * @since 2025-05-10
 */
@Slf4j
@Component
public class LogInterceptor implements HandlerInterceptor {

	/**
	 * 요청 처리 전에 TraceId를 생성하고 MDC에 설정한다.
	 *
	 * @param request  HttpServletRequest 요청 객체
	 * @param response HttpServletResponse 응답 객체
	 * @param handler  Object 핸들러 객체
	 * @return boolean 처리 계속 여부
	 * @author 정안식
	 * @since 2025-05-10
	 */
	@Override
	public boolean preHandle(HttpServletRequest request,
		HttpServletResponse response, Object handler) {
		String traceId = generateTraceId();
		setTraceId(traceId);
		log.info("TraceId 생성 성공 - " + traceId);
		return true;
	}

	/**
	 * 요청 처리 완료 후 MDC에 설정된 TraceId를 제거한다.
	 *
	 * @param request  HttpServletRequest 요청 객체
	 * @param response HttpServletResponse 응답 객체
	 * @param handler  Object 핸들러 객체
	 * @param ex       Exception 발생 예외 객체
	 * @author 정안식
	 * @since 2025-05-10
	 */
	@Override
	public void afterCompletion(HttpServletRequest request,
		HttpServletResponse response, Object handler, Exception ex) {
		clearTraceId();
	}

	/**
	 * 새로운 UUID 형식의 TraceId를 생성한다.
	 *
	 * @return String 생성된 TraceId 문자열
	 * @author 정안식
	 * @since 2025-05-10
	 */
	private String generateTraceId() {
		return UUID.randomUUID().toString();
	}

	/**
	 * 생성된 TraceId를 MDC에 설정한다.
	 *
	 * @param traceId String 설정할 TraceId
	 * @author 정안식
	 * @since 2025-05-10
	 */
	private void setTraceId(String traceId) {
		MDC.put("traceId", traceId);
	}

	/**
	 * MDC에 설정된 모든 정보를 제거하여 메모리 누수를 방지한다.
	 *
	 * @author 정안식
	 * @since 2025-05-10
	 */
	private void clearTraceId() {
		MDC.clear();
	}
}
