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

@Component
@RequiredArgsConstructor
public class ChatBatchQuartzLauncher implements org.quartz.Job {

	private final JobLauncher jobLauncher;

	@Qualifier("chatFlushJob")
	private final Job chatFlushJob;

	@Override
	public void execute(JobExecutionContext context) {
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