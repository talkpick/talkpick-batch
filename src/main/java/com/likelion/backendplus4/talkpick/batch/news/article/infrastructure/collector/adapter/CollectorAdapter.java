package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.adapter;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.application.port.out.CollectorPort;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;

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
	 * @throws ArticleCollectorException SchedulerException 발생으로 실행 실패 시
	 * @author 함예정
	 * @since 2025-05-10
	 */
	@Override
	public boolean start() {
		try {
			scheduler.start();
			return isRunning();
		} catch (SchedulerException e) {
			throw new ArticleCollectorException(ArticleCollectorErrorCode.SCHEDULER_START_FAIL);
		}
	}

	/**
	 * Quartz 스케줄러를 standby 상태로 전환하여 정지한다. <p>
	 * 이미 standby 상태인 경우에는 아무 작업도 하지 않는다.
	 *
	 * @return 정지 요청이 성공했으면 true
	 * @throws ArticleCollectorException 스케줄러가 비정상 상태이거나 standby 전환 실패 시
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
			throw new ArticleCollectorException(ArticleCollectorErrorCode.SCHEDULER_STOP_FAIL);
		}
	}

	/**
	 * 스케줄러가 실행 중인지 최대 10회 (3초) 반복 확인한다. <p>
	 * 각 확인 사이에 300ms 지연을 두고, 실행 중이면 즉시 true 반환.
	 *
	 * @return 스케줄러가 실행 중이면 true, 그렇지 않으면 false
	 * @throws ArticleCollectorException 스케줄러 상태 확인 실패 시
	 * @author 함예정
	 * @since 2025-05-10
	 */
	@Override
	public boolean isRunning() {
		for (int i = 0; i < 10; i++) {
			try {
				if (scheduler.isStarted() && !scheduler.isInStandbyMode()) {
					return true;
				}
				sleep(300L);
			} catch (SchedulerException e) {
				throw new ArticleCollectorException(ArticleCollectorErrorCode.STATUS_CHECK_FAIL);
			}
		}
		return false;
	}

	/**
	 * 현재 스레드를 지정된 시간(밀리초) 동안 일시 중단한다.  <p>
	 * 중단(interrupt)될 경우, 인터럽트 상태를 복원하여 호출자에게 중단 신호를 전달한다.
	 *
	 * @param time 일시 중단할 시간 (단위: 밀리초)
	 * @author 함예정
	 * @since 2025-05-10
	 */
	private void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
