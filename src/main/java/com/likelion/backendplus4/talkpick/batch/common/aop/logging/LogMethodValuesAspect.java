package com.likelion.backendplus4.talkpick.batch.common.aop.logging;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.common.annotation.logging.LogMethodValues;

import lombok.extern.slf4j.Slf4j;

/**
 * LogMethodValues 애노테이션이 적용된 메서드의 인자와 반환값을 로그로 기록하는 AOP 클래스
 *
 * @since 2025-05-10
 */
@Aspect
@Component
@Slf4j
@Order(1)
public class LogMethodValuesAspect {

	/**
	 * LogMethodValues 애노테이션이 적용된 메서드의 인자와 반환값을 로그로 출력한다.
	 *
	 * @param pjp 실행 중인 JoinPoint
	 * @param logMethodValues LogMethodValues 애노테이션 정보
	 * @return 메서드 실행 결과
	 * @throws Throwable 실행 중 발생한 예외
	 * @author 정안식
	 * @since 2025-05-10
	 */
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
