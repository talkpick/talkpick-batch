package com.likelion.backendplus4.talkpick.batch.chat.infrastructure.adapter.out.batch;


import org.quartz.*;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatBatchQuartzLauncher implements Job {

	private final JobLauncher jobLauncher;

	@Qualifier("chatFlushJob")
	private final org.springframework.batch.core.Job chatFlushJob;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			jobLauncher.run(chatFlushJob,
				new JobParametersBuilder()
					.addLong("ts", System.currentTimeMillis())
					.toJobParameters());
		} catch (Exception e) {
			throw new JobExecutionException("batch failure", e);
		}
	}
}