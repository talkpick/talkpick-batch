package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.quartz.trigger;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GlobalJobListener implements JobListener {

	@Override
	public String getName() {
		return "globalJobListener";
	}

	// 잡 실행 직전
	@Override
	public void jobToBeExecuted(JobExecutionContext context) {
		log.info("🔄 실행 예정: " + context.getJobDetail().getKey());
	}

	// 실행이 취소됐을 때 (TriggerListener가 veto했을 때)
	@Override
	public void jobExecutionVetoed(JobExecutionContext context) {
		log.info("⛔ 실행 취소됨: " + context.getJobDetail().getKey());
	}

	// 잡 실행 완료 (성공/실패 모두 포함)
	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		if (jobException != null) {
			log.error("🚨 잡 실패: " + context.getJobDetail().getKey());
			jobException.printStackTrace();
		} else {
			log.error("✅ 잡 성공: " + context.getJobDetail().getKey());
		}
	}
}
