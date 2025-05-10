package com.likelion.backendplus4.talkpick.batch.common.decorator.logging;

import java.util.Map;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.stereotype.Component;

/**
 * 스레드 풀에서 실행되는 Task에 MDC(Context Map)를 전파하기 위한 TaskDecorator 구현체
 * MDC 정보를 부모 스레드에서 자식 스레드로 복사하여 로그 추적 정보를 유지하도록 한다.
 *
 * @since 2025-05-10
 */
@Component
public class MdcTaskDecorator implements TaskDecorator {

	/**
	 * Runnable 실행 시 부모 스레드의 MDC(Context Map)를 자식 스레드로 복사하여 설정한다.
	 * 실행 후 MDC를 반드시 clear하여 메모리 누수를 방지한다.
	 *
	 * @param runnable 실행할 원본 Runnable
	 * @return MDC context를 설정한 새로운 Runnable
	 * @author 정안식
	 * @since 2025-05-10
	 */
	@Override
	public Runnable decorate(Runnable runnable) {
		Map<String, String> contextMap = MDC.getCopyOfContextMap();
		return () -> {
			if (contextMap != null) {
				MDC.setContextMap(contextMap);
			}
			try {
				runnable.run();
			} finally {
				MDC.clear();
			}
		};
	}
}