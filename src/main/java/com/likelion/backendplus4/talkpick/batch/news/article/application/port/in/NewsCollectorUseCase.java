package com.likelion.backendplus4.talkpick.batch.news.article.application.port.in;

import com.likelion.backendplus4.talkpick.batch.news.article.application.service.dto.CollectorStatusResponse;

/**
 * 뉴스 RSS 수집 실행/정지를 위한 유스케이스 인터페이스.
 * RSS 수집기를 제어하고 현재 상태 정보를 반환한다.
 *
 * @since 2025-05-10
 */
public interface NewsCollectorUseCase {

	/**
	 * 수집 스케줄을 시작한다.
	 *
	 * @return 수집기의 상태 정보를 담은 응답 객체
	 */
	CollectorStatusResponse start();

	/**
	 * 뉴스 스케줄을 중단한다.
	 *
	 * @return 수집기의 상태 정보를 담은 응답 객체
	 */
	CollectorStatusResponse stop();
}
