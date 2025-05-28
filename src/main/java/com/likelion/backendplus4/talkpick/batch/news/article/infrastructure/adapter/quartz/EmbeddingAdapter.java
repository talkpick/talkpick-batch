package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.quartz;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.quartz.exception.IndexException;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.quartz.exception.error.IndexErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.quartz.job.SummaryAndEmbeddingQuartzJobDetail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmbeddingAdapter implements EmbeddingPort {
	private final Scheduler scheduler;

	@Override
	public void startEmbedding() {
		startIndexQuartzJob(SummaryAndEmbeddingQuartzJobDetail.ARTICLE_EMBEDDING_JOB_DETAIL_NAME);
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
