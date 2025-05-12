package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.adapter;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.application.port.out.CollectorPort;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * Quartz Scheduler 를 제어하는 CollectorPort 구현체.
 * 외부 요청에 따라 스케줄러를 시작하거나 정지하며,
 * 현재 실행 중인지 상태를 확인할 수 있다.
 *
 * @since 2025-05-10
 */
@Component
@RequiredArgsConstructor
public class CollectorAdapter implements CollectorPort {
	private final Scheduler scheduler;

	/**
	 * Quartz 스케줄러를 시작하고, 정상적으로 시작되었는지 상태를 확인한다.
	 *
	 * @return 스케줄러가 실행 중이면 true, 그렇지 않으면 false
	 * @author 함예정
	 * @since 2025-05-10
	 */
	@Override
	public boolean start() {
		return startScheduler();
	}

	/**
	 * Quartz 스케줄러를 standby 상태로 전환하여 정지한다. <p>
	 * 이미 standby 상태인 경우에는 아무 작업도 하지 않는다.
	 *
	 * @return 정지 요청이 성공했으면 true
	 * @author 함예정
	 * @since 2025-05-10
	 */
	@Override
	public boolean stop() {
		try {
			if (!scheduler.isInStandbyMode()) {
				scheduler.standby();
			}
			return true;
		} catch (SchedulerException e) {
			throw new ArticleCollectorException(ArticleCollectorErrorCode.SCHEDULER_STOP_FAIL, e);
		}
	}

	/**
	 * 스케줄러가 실행 상태 플래그 확인 메소드
	 *
	 * @return 스케줄러가 실행 중이면 true, 그렇지 않으면 false
	 * @author 함예정
	 * @since 2025-05-10
	 */
	@Override
	public boolean isRunning() {
		return checkSchedulerStatus();
	}

	/**
	 * Quartz 스케줄러를 세부 시작 메소드
	 * 1. 실행 요청
	 * 2. 실행 상태 플래그 반환
	 *
	 * @return 스케줄러가 실행 중이면 true, 그렇지 않으면 false
	 * @throws ArticleCollectorException SchedulerException 발생으로 실행 실패 시
	 * @author 함예정
	 * @since 2025-05-11
	 */
	private boolean startScheduler() {
		try {
			scheduler.start();
			return isRunning();
		} catch (SchedulerException e) {
			throw new ArticleCollectorException(ArticleCollectorErrorCode.SCHEDULER_START_FAIL, e);
		}
	}

	/**
	 * 스케줄러의 현재 상태를 확인합니다.
	 *
	 * @return 스케줄러가 시작되었고 대기 모드가 아닌 경우 true, 그렇지 않으면 false
	 * @throws ArticleCollectorException 스케줄러 상태 확인 중 예외 발생 시 커스텀 예외로 래핑하여 던짐
	 * @author 함예정
	 * @since 2025-05-11
	 */
	private boolean checkSchedulerStatus() {
		try {
			return scheduler.isStarted() && !scheduler.isInStandbyMode();
		} catch (SchedulerException e) {
			throw new ArticleCollectorException(ArticleCollectorErrorCode.STATUS_CHECK_FAIL, e);
		}
	}
}
