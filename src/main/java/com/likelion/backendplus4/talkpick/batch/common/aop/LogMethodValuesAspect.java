package com.likelion.backendplus4.talkpick.batch.common.aop;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.common.annotation.logging.LogMethodValues;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
@Order(1)
public class LogMethodValuesAspect {

	@Around("@annotation(logMethodValues)")
	public Object logArgsAndReturn(ProceedingJoinPoint pjp, LogMethodValues logMethodValues) throws Throwable {
		String className = pjp.getTarget().getClass().getSimpleName();
		String method = pjp.getSignature().toShortString();
		String logLevel = logMethodValues.logLevel().toLowerCase();

		log(logLevel, "{}-{}메서드[ARGS] - {}", className, method, Arrays.toString(pjp.getArgs()));

		Object result = pjp.proceed();

		log(logLevel, "{}-{}메서드[RETURN] - {}", className, method, result);
		return result;
	}

	private void log(String logLevel, String format, Object... args) {
		if ("debug".equals(logLevel)) {
			log.debug(format, args);
		} else {
			log.info(format, args);
		}
	}
}
