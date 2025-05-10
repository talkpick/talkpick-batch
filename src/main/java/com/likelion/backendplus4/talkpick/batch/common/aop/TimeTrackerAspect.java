package com.likelion.backendplus4.talkpick.batch.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.common.annotation.logging.TimeTracker;

import lombok.extern.slf4j.Slf4j;

/**
 * TimeTracker 애노테이션이 적용된 메서드의 실행 시간을 측정하여 로그로 남기는 AOP 클래스
 *
 * @since 2025-05-10
 */
@Aspect
@Component
@Slf4j
@Order(2)
public class TimeTrackerAspect {

	/**
	 * TimeTracker 애노테이션이 적용된 메서드를 감싸서 실행 시간을 기록하고 로그를 출력한다.
	 *
	 * @param pjp 실행 중인 JoinPoint
	 * @param timeTracker TimeTracker 애노테이션 정보
	 * @return 메서드 실행 결과
	 * @throws Throwable 실행 중 발생한 예외
	 * @author 정안식
	 * @since 2025-05-10
	 */
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
