package com.likelion.backendplus4.talkpick.batch.chat.infrastructure.adapter.out.batch.config;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.likelion.backendplus4.talkpick.batch.chat.infrastructure.adapter.out.batch.ChatBatchQuartzLauncher;

import lombok.RequiredArgsConstructor;

/**
 * Quartz 스케줄러를 통해 채팅 배치 플러시 작업을 주기적으로 실행하도록 설정하는 구성 클래스입니다.
 *
 * @since 2025-05-27
 */
@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing
public class QuartzChatConfig {

    private static final String JOB_NAME = "chatFlushQuartzJob";
    private static final String TRIGGER_NAME = "chatFlushQuartzTrigger";
    private static final String GROUP_NAME = "chatGroup";

	@Value("${chat.flush.interval}")
	private Long flushDelayMillis;

	/**
	 * 채팅 플러시 배치 작업(JobDetail)을 생성하여 Quartz에 등록합니다.
	 *
	 * @return 채팅 플러시용 JobDetail 객체
	 * @author 박찬병
	 * @since 2025-05-27
	 */
	@Bean
	public JobDetail chatFlushJobDetail() {
		return JobBuilder.newJob(ChatBatchQuartzLauncher.class)
			.withIdentity(JOB_NAME, GROUP_NAME)
			.storeDurably()
			.build();
	}

	/**
	 * 채팅 플러시 트리거(Trigger)를 생성하여 지정된 간격으로 배치 작업을 실행하도록 설정합니다.
	 *
	 * @param chatFlushJobDetail 생성된 채팅 플러시 JobDetail
	 * @return 배치 작업 실행을 위한 Trigger 객체
	 * @author 박찬병
	 * @since 2025-05-27
	 */
	@Bean
	public Trigger chatFlushTrigger(JobDetail chatFlushJobDetail) {
		return TriggerBuilder.newTrigger()
			.forJob(chatFlushJobDetail)
			.withIdentity(TRIGGER_NAME, GROUP_NAME)
			.startNow()
			.withSchedule(SimpleScheduleBuilder
				.simpleSchedule()
				.withIntervalInMilliseconds(flushDelayMillis)
				.repeatForever()
			)
			.build();
	}
}