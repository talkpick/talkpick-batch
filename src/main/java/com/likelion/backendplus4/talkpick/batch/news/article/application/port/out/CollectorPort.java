package com.likelion.backendplus4.talkpick.batch.news.article.application.port.out;

/**
 * 뉴스 수집 스케줄 제어를 위한 외부 시스템 연동 포트 인터페이스. <p>
 * Quartz Scheduler 등의 외부 실행 환경을 시작/정지하거나 상태를 확인할 수 있도록 정의한다. <p><p>
 *
 * 이 포트는 Adapter를 통해 실제 구현되며, <p>
 * 유스케이스 계층에서는 이 인터페이스만 의존한다.
 *
 * @since 2025-05-10
 */
public interface CollectorPort {
	/**
	 * 스케줄 실행을 요청한다.
	 *
	 * @return 실행 요청이 성공하고 실제로 실행 중이면 true
	 */
	boolean start();

	/**
	 * 스케줄 정지를 요청한다.
	 *
	 * @return 정지 요청이 성공하면 true
	 */
	boolean stop();


	/**
	 * 현재 실행 중인지 확인한다.
	 *
	 * @return 실행 중이면 true, 아니면 false
	 */
	boolean isRunning();
}
