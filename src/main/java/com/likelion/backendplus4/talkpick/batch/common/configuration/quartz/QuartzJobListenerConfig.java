package com.likelion.backendplus4.talkpick.batch.common.configuration.quartz;

import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Quartz 스케줄러의 글로벌 JobListener 설정을 위한 구성 클래스입니다.
 *
 * @since 2025-05-29
 */
@Configuration
public class QuartzJobListenerConfig {

	/**
	 * SchedulerFactoryBeanCustomizer 빈을 등록하여,
	 * Quartz 스케줄러에 글로벌 JobListener를 설정합니다.
	 *
	 * @return SchedulerFactoryBeanCustomizer 인스턴스
	 * @author 함예정
	 * @since 2025-05-29
	 */
	@Bean
	public SchedulerFactoryBeanCustomizer schedulerFactoryBean() {
		return schedulerFactoryBean
			-> schedulerFactoryBean.setGlobalJobListeners(new QuartzGlobalJobListener());
	}
}
