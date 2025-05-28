package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.quartz;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.application.port.out.NewsInfoIndexRepositoryPort;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.quartz.exception.IndexException;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.quartz.exception.error.IndexErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.quartz.job.IndexQuartzJobDetail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class IndexAdapter implements NewsInfoIndexRepositoryPort {
	private final Scheduler scheduler;

	@Override
	public void saveAll() {
		startIndexQuartzJob(IndexQuartzJobDetail.indexJobDetailName);
	}

	private void startIndexQuartzJob(String jobName) {
		JobKey jobKey = JobKey.jobKey(jobName);
		if (checkJobExists(jobKey)) {
			try {
				scheduler.triggerJob(jobKey);
			} catch (SchedulerException e) {
				throw new IndexException(IndexErrorCode.TRIGGER_MISS_FIRE);
			}
		}
	}

	private boolean checkJobExists(JobKey jobKey) {
		try {
			return scheduler.checkExists(jobKey);
		} catch (SchedulerException e) {
			log.warn("해당 Job은 등록되어있지 않습니다. JobName: {}", jobKey.getName());
		}
		return false;
	}
}
