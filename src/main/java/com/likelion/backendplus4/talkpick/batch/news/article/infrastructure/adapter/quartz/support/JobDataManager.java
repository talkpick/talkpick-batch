package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.quartz.support;

import java.util.function.Supplier;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

/**
 * Quartz JobDataMap에 대한 접근을 캡슐화하는 유틸리티 클래스입니다.
 * Job 실행 시 데이터의 저장과 조회를 간결하게 처리할 수 있도록 도와줍니다.
 *
 * @since 2025-05-29
 */
public class JobDataManager {
	private final JobDataMap jobDataMap;

	public JobDataManager(JobExecutionContext jobExecutionContext) {
		jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
	}

	/**
	 * JobDataMap에서 지정된 키의 long 값을 조회합니다.
	 * 값이 존재하지 않을 경우, 지정된 Supplier를 통해 기본 값을 반환합니다.
	 *
	 * @param key 조회할 키
	 * @param defaultSupplier 기본 값을 제공하는 Supplier
	 * @return 존재하는 long 값 또는 기본 값 (기본 값이 null이면 1L 반환)
	 * @author 함예정
	 * @since 2025-05-29
	 */
	public long getLong(String key, Supplier<Long> defaultSupplier) {
		if (jobDataMap.containsKey(key)) {
			return jobDataMap.getLong(key);
		}
		Long defaultValue = defaultSupplier.get();
		return defaultValue != null ? defaultValue : 1L;
	}

	/**
	 * JobDataMap에 long 값을 저장합니다.
	 *
	 * @param key 저장할 키
	 * @param value 저장할 long 값
	 * @author 함예정
	 * @since 2025-05-29
	 */
	public void saveLong(String key, long value) {
		jobDataMap.put(key, value);
	}
}
