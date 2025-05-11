package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.executor;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;

import lombok.RequiredArgsConstructor;

/**
 * Quartz에 의해 트리거되는 Spring Batch Job 실행 클래스.
 * JobLauncher를 통해 {@code rssJob}을 수동 실행하며, 매 실행 시 고유한 JobParameters를 생성하여 중복 실행을 방지한다.
 *
 * - @DisallowConcurrentExecution: 이전 실행이 끝나기 전에는 새로운 실행이 중첩되지 않도록 제한
 * - JobParameters에 timestamp를 포함시켜 매번 다른 인스턴스로 실행되도록 설정
 *
 * 이 클래스는 단순한 실행자 역할만 하며, 실제 배치 로직은 {@code rssJob} 내부에 정의되어 있다.
 *
 * @since 2025-05-10
 */
@Component
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class BatchJobExecutor implements org.quartz.Job {
	private final JobLauncher jobLauncher;
	private final Job articleCollectorBatchJob;
	@Override
	public void execute(JobExecutionContext jobExecutionContext) {
		startSpringBatchJob();
	}

	private void startSpringBatchJob() {
		JobParameters params = new JobParametersBuilder()
			.addLong("timestamp", System.currentTimeMillis())
			.toJobParameters();

		try {
			jobLauncher.run(articleCollectorBatchJob, params);
		} catch (JobExecutionAlreadyRunningException e) {
			throw new ArticleCollectorException(ArticleCollectorErrorCode.JOB_ALREADY_RUNNING);
		} catch (JobRestartException e) {
			throw new ArticleCollectorException(ArticleCollectorErrorCode.JOB_RESTART_FAIL);
		} catch (JobInstanceAlreadyCompleteException e) {
			throw new ArticleCollectorException(ArticleCollectorErrorCode.JOB_ALREADY_COMPLETE);
		} catch (JobParametersInvalidException e) {
			throw new ArticleCollectorException(ArticleCollectorErrorCode.INVALID_JOB_PARAMETER);
		} catch (Exception e) {
			throw new ArticleCollectorException(ArticleCollectorErrorCode.UNKNOWN_ERROR);
		}
	}
}
