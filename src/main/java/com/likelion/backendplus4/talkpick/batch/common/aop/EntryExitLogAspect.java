package com.likelion.backendplus4.talkpick.batch.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import com.likelion.backendplus4.talkpick.batch.common.annotation.logging.EntryExitLog;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class EntryExitLogAspect {

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

	private void log(String logLevel, String format, Object... args) {
		if ("debug".equals(logLevel)) {
			log.debug(format, args);
		} else {
			log.info(format, args);
		}
	}
}
