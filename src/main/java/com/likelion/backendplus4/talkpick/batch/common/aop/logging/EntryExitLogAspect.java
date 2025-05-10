package com.likelion.backendplus4.talkpick.batch.common.aop.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.common.annotation.logging.EntryExitLog;

import lombok.extern.slf4j.Slf4j;

/**
 * EntryExitLog 애노테이션이 적용된 메서드의 진입과 종료 시점을 로그로 기록하는 AOP 클래스
 *
 * @since 2025-05-10
 */
@Aspect
@Component
@Slf4j
@Order(1)
public class EntryExitLogAspect {

	/**
	 * EntryExitLog 애노테이션이 적용된 메서드를 감싸서 시작 전과 종료 후에 로그를 출력한다.
	 *
	 * @param pjp          실행 중인 JoinPoint
	 * @param entryExitLog EntryExitLog 애노테이션 정보
	 * @return 메서드 실행 결과 객체
	 * @throws Throwable   메서드 실행 중 발생한 예외
	 * @author 정안식
	 * @since 2025-05-10
	 */
	@Around("@annotation(entryExitLog)")
	public Object logAround(ProceedingJoinPoint pjp, EntryExitLog entryExitLog) throws Throwable {
		String className = pjp.getTarget().getClass().getSimpleName();
		String method = pjp.getSignature().toShortString();
		String logLevel = entryExitLog.logLevel().toLowerCase();
		log(logLevel, "{}-{} 메서드 시작", className, method);

		Object result = pjp.proceed();

		log(logLevel, "{}-{} 메서드 종료", className, method);
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
