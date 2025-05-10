package com.likelion.backendplus4.talkpick.batch.common.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.likelion.backendplus4.talkpick.batch.common.interceptor.LogInterceptor;

import lombok.RequiredArgsConstructor;

/**
 * 인터셉터를 등록하는 Web MVC 설정 클래스
 *
 * @since 2025-05-10
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

	private static final String ALL_PATTERN = "/**";

	private final LogInterceptor logInterceptor;

	/**
	 * LogInterceptor를 모든 경로에 등록한다.
	 *
	 * @param registry InterceptorRegistry 인터셉터 레지스트리
	 * @author 정안식
	 * @since 2025-05-10
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(logInterceptor)
			.addPathPatterns(ALL_PATTERN);
	}
}
