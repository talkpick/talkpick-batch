package com.likelion.backendplus4.talkpick.batch.chat.infrastructure.adapter.out.batch;

import org.quartz.JobExecutionContext;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import com.likelion.backendplus4.talkpick.batch.chat.exception.ChatBatchException;
import com.likelion.backendplus4.talkpick.batch.chat.exception.error.ChatBatchErrorCode;

/**
 * Quartz 스케줄러의 실행 시점에 Spring Batch의 chatFlushJob을 호출하는 Job 구현체입니다.
 *
 * @since 2025-05-27
 */
@Component
@RequiredArgsConstructor
public class ChatBatchQuartzLauncher implements org.quartz.Job {

	private final JobLauncher jobLauncher;

	@Qualifier("chatFlushJob")
	private final Job chatFlushJob;

	/**
	 * Quartz Job 실행 시 Spring Batch chatFlushJob을 실행합니다.
	 *
	 * @param context Quartz JobExecutionContext
	 * @throws ChatBatchException 배치 실행 중 오류 발생 시 예외를 던집니다.
	 * @author 박찬병
	 * @since 2025-05-27
	 */
	@Override
	public void execute(JobExecutionContext context) {
	    runChatFlushJob();
	}

	/**
	 * Spring Batch chatFlushJob을 실행하고, 실행 중 예외가 발생하면 ChatBatchException을 던집니다.
	 *
	 * @author 박찬병
	 * @since 2025-05-27
	 */
	private void runChatFlushJob() {
	    try {
	        jobLauncher.run(chatFlushJob,
	            new JobParametersBuilder()
	                .addLong("ts", System.currentTimeMillis())
	                .toJobParameters());
	    } catch (Exception e) {
	        throw new ChatBatchException(ChatBatchErrorCode.BATCH_LAUNCH_ERROR, e);
	    }
	}
}