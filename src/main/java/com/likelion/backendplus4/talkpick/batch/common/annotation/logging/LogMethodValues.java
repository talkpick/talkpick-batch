package com.likelion.backendplus4.talkpick.batch.common.annotation.logging;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메서드의 진입 및 종료 시점을 기록하기 위한 애노테이션
 *
 * @since 2025-05-10
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogMethodValues {
	/**
	 * 기록할 로그 레벨을 지정한다.
	 *
	 * @return 로그 레벨 문자열 (예: "debug", "info")
	 * @since 2025-05-10
	 */
	String logLevel() default "info";
}
