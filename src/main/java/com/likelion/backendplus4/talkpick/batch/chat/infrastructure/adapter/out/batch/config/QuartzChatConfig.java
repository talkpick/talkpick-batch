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

@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing
public class QuartzChatConfig {

	@Value("${chat.flush.delay}")
	private Long flushDelayMillis;


	@Bean
	public JobDetail chatFlushJobDetail() {
		return JobBuilder.newJob(ChatBatchQuartzLauncher.class)
			.withIdentity("chatFlushQuartzJob", "chatGroup")
			.storeDurably()
			.build();
	}

	@Bean
	public Trigger chatFlushTrigger(JobDetail chatFlushJobDetail) {
		return TriggerBuilder.newTrigger()
			.forJob(chatFlushJobDetail)
			.withIdentity("chatFlushQuartzTrigger", "chatGroup")
			.startNow()
			.withSchedule(SimpleScheduleBuilder
				.simpleSchedule()
				.withIntervalInMilliseconds(flushDelayMillis)
				.repeatForever()
			)
			.build();
	}
}
