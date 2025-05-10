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

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
@Order(3)
public class LogJsonAspect {

	private final ObjectMapper objectMapper;

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

	private void logJsonSafely(String logLevel, String format, String className, String method, Object target) {
		try {
			String json = objectMapper.writeValueAsString(target);
			log(logLevel, format, className, method, json);
		} catch (Exception e) {
			log.warn("{}-{} 메서드 JSON 변환 실패", className, method, e);
		}
	}

	private void log(String logLevel, String format, Object... args) {
		if ("debug".equals(logLevel)) {
			log.debug(format, args);
		} else {
			log.info(format, args);
		}
	}
}
