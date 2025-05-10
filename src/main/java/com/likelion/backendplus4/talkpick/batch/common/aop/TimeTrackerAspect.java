package com.likelion.backendplus4.talkpick.batch.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.common.annotation.logging.TimeTracker;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class TimeTrackerAspect {

	@Around("@annotation(timeTracker)")
	public Object trackTime(ProceedingJoinPoint pjp, TimeTracker timeTracker) throws Throwable {
		String method = pjp.getSignature().toShortString();
		String logLevel = timeTracker.logLevel().toLowerCase();

		long start = System.currentTimeMillis();

		Object result = pjp.proceed();

		long elapsedMillis = System.currentTimeMillis() - start;
		double elapsedSeconds = elapsedMillis / 1000.0;
		String formatted = String.format("%.3f", elapsedSeconds);

		log(logLevel, "{} 실행 시간 = {} 초", method, formatted);
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
