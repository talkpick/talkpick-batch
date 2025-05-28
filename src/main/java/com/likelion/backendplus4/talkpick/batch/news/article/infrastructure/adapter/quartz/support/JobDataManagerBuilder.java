package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.quartz.support;

import org.quartz.JobExecutionContext;

/**
 * JobExecutionContext를 기반으로 JobDataManager 인스턴스를 생성하는 빌더 클래스
 *
 * @since 2025-05-29
 */
public class JobDataManagerBuilder {
	
	/**
	 * JobExecutionContext를 기반으로 JobDataManager 인스턴스를 반환
	 *
	 * @param jobExecutionContext Quartz 작업 실행 컨텍스트
	 * @return JobDataManager 인스턴스
	 * @author 함예정
	 * @since 2025-05-29
	 */
	public static JobDataManager buildJobManager(JobExecutionContext jobExecutionContext) {
		return new JobDataManager(jobExecutionContext);
	}
}
