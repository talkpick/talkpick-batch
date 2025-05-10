package com.likelion.backendplus4.talkpick.batch.common.annotation.logging;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TimeTracker {
	String logLevel() default "info";
}
