package com.likelion.backendplus4.talkpick.batch.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.backendplus4.talkpick.batch.common.annotation.logging.LogJson;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * LogJson 애노테이션이 적용된 메서드의 입력값과 반환값을 JSON 형식으로 변환하여 로그로 기록하는 AOP 클래스
 *
 * @since 2025-05-10
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
@Order(3)
public class LogJsonAspect {

	private final ObjectMapper objectMapper;

	/**
	 * LogJson 애노테이션이 적용된 메서드의 입력값과 반환값을 JSON 문자열로 변환하여 로그로 출력한다.
	 *
	 * @param pjp 실행 중인 JoinPoint
	 * @param logJson LogJson 애노테이션 정보
	 * @return 메서드 실행 결과
	 * @throws Throwable 실행 중 발생한 예외
	 * @author 정안식
	 * @since 2025-05-10
	 */
	@Around("@annotation(logJson)")
	public Object logJson(ProceedingJoinPoint pjp, LogJson logJson) throws Throwable {
		String className = pjp.getTarget().getClass().getSimpleName();
		String method = pjp.getSignature().toShortString();
		String logLevel = logJson.logLevel().toLowerCase();

		logJsonSafely(logLevel, "{}-{} 메서드 [JSON 입력값] - {}", className, method, pjp.getArgs());

		Object result = pjp.proceed();

		logJsonSafely(logLevel, "{}-{} 메서드 [JSON 반환값] - {}", className, method, result);

		return result;
	}

	/**
	 * JSON 변환에 실패하지 않도록 안전하게 로그를 출력한다.
	 *
	 * @param logLevel  로그 레벨 (debug 또는 info)
	 * @param format    로그에 출력할 메시지 포맷
	 * @param className 클래스 이름 문자열
	 * @param method    메서드 정보 문자열
	 * @param target    변환 대상 객체
	 * @author 정안식
	 * @since 2025-05-10
	 */
	private void logJsonSafely(String logLevel, String format, String className, String method, Object target) {
		try {
			String json = objectMapper.writeValueAsString(target);
			log(logLevel, format, className, method, json);
		} catch (Exception e) {
			log.warn("{}-{} 메서드 JSON 변환 실패", className, method, e);
		}
	}

	/**
	 * 지정된 로그 레벨에 따라 메시지를 출력한다.
	 *
	 * @param logLevel 로그 레벨 (debug 또는 info)
	 * @param format 출력할 메시지 포맷
	 * @param args 포맷에 전달할 인자
	 * @author 정안식
	 * @since 2025-05-10
	 */
	private void log(String logLevel, String format, Object... args) {
		if ("debug".equals(logLevel)) {
			log.debug(format, args);
		} else {
			log.info(format, args);
		}
	}
}
