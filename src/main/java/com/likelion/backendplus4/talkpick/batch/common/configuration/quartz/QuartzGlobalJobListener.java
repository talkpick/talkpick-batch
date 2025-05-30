package com.likelion.backendplus4.talkpick.batch.common.configuration.quartz;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

import lombok.extern.slf4j.Slf4j;

/**
 * Quartz JobListener 구현체로,
 * 모든 Job 실행에 대한 로그를 처리하는 글로벌 리스너입니다.
 *
 * @since 2025-05-29
 */
@Slf4j
public class QuartzGlobalJobListener implements JobListener {

	/**
	 * 이 JobListener의 이름을 반환합니다.
	 * Quartz에서 리스너를 식별하기 위한 고유 이름입니다.
	 *
	 * @return 리스너 이름
	 * @author 함예정
	 * @since 2025-05-29
	 */
	@Override
	public String getName() {
		return "globalJobListener";
	}

	/**
	 * Quartz Job이 실행되기 직전에 호출되며,
	 * 실행 대기 중인 Job에 대한 로그를 출력합니다.
	 *
	 * @param context 현재 실행될 Job의 실행 컨텍스트
	 * @author 함예정
	 * @since 2025-05-29
	 */
	@Override
	public void jobToBeExecuted(JobExecutionContext context) {

		log.info("🔄 실행 대기: " + context.getJobDetail().getKey());
	}

	/**
	 * Quartz Job 실행이 취소되었을 때 호출되며,
	 * TriggerListener에서 veto가 발생한 경우에 실행됩니다.
	 *
	 * @param context 실행이 취소된 Job의 실행 컨텍스트
	 * @author 함예정
	 * @since 2025-05-29
	 */
	@Override
	public void jobExecutionVetoed(JobExecutionContext context) {
		log.info("⛔ 실행 취소: " + context.getJobDetail().getKey());
	}

	/**
	 * Quartz Job 실행이 완료된 후 호출되며,
	 * 성공 또는 실패 여부에 따라 로그를 출력합니다.
	 *
	 * @param context 실행된 Job의 실행 컨텍스트
	 * @param jobException Job 실행 중 발생한 예외, 예외가 없으면 null
	 * @author 함예정
	 * @since 2025-05-29
	 */
	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		if (jobException != null) {
			log.error("🚨 잡 실패: " + context.getJobDetail().getKey());
			jobException.printStackTrace();
		} else {
			log.info("✅ 잡 성공: " + context.getJobDetail().getKey());
		}
	}
}
