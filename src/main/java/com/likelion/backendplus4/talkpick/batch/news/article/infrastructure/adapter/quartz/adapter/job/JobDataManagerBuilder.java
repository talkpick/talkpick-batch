package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.quartz.adapter.job;

import org.quartz.JobExecutionContext;

public class JobDataManagerBuilder {
	public static JobDataManager getInstance(JobExecutionContext jobExecutionContext) {
		return new JobDataManager(jobExecutionContext);
	}
}
