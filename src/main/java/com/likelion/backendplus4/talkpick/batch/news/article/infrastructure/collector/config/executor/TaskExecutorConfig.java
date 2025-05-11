package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.likelion.backendplus4.talkpick.batch.common.decorator.logging.MdcTaskDecorator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class TaskExecutorConfig {
	private final MdcTaskDecorator mdcTaskDecorator;

	@Getter
	private static final String normalExecutorName = "normalExecutor";

	/**
	 * 일반적인 작업처리를 위한 ThreadPool 기반 TaskExecutor 설정
	 *
	 * @return TaskExecutor 인스턴스
	 * @author 함예정
	 * @since 2025-05-02
	 */
	@Bean(normalExecutorName)
	public TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(10);
		executor.setTaskDecorator(mdcTaskDecorator);
		executor.setThreadNamePrefix("normalExecutor-");
		executor.initialize();
		return executor;
	}
}
