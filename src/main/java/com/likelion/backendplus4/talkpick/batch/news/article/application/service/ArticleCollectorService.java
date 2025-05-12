package com.likelion.backendplus4.talkpick.batch.news.article.application.service;

import org.springframework.stereotype.Service;

import com.likelion.backendplus4.talkpick.batch.news.article.application.port.in.ArticleCollectorUseCase;
import com.likelion.backendplus4.talkpick.batch.news.article.application.port.out.CollectorPort;
import com.likelion.backendplus4.talkpick.batch.news.article.application.service.dto.ArticleCollectorStatusResponse;

import lombok.RequiredArgsConstructor;

/**
 * 뉴스 기사 수집 스케줄러를 제어하는 유스케이스 구현체.
 * 수집기 실행 및 중단 요청을 처리하고, 그 결과를 상태 응답으로 반환한다.
 *
 * 내부적으로 {@link CollectorPort}를 호출하여 Quartz Scheduler 상태를 제어하며,
 * 실행 결과에 따라 성공/실패 메시지를 포함한 {@link ArticleCollectorStatusResponse}를 생성한다.
 *
 * @author 함예정
 * @since 2025-05-11
 */
@Service
@RequiredArgsConstructor
public class ArticleCollectorService implements ArticleCollectorUseCase {
	private final CollectorPort collectorPort;

	/**
	 * 수집기를 실행한다.
	 * 실행 성공 여부에 따라 상태 응답을 생성한다.
	 *
	 * @return 실행 결과에 대한 상태 응답
	 * @author 함예정
	 * @since 2025-05-11
	 */
	@Override
	public ArticleCollectorStatusResponse start() {
		boolean result = collectorPort.start();
		return getCollectorStatusResponse(result);
	}

	/**
	 * 수집기를 정지한다.
	 * 정지 성공 여부에 따라 상태 응답을 생성한다.
	 *
	 * @return 정지 결과에 대한 상태 응답
	 * @author 함예정
	 * @since 2025-05-11
	 */
	@Override
	public ArticleCollectorStatusResponse stop() {
		boolean result = collectorPort.stop();
		return getCollectorStatusResponse(result);
	}

	/**
	 * 실행 결과에 따라 응답 메시지를 구성한다.
	 *
	 * @param result CollectorPort 실행 결과
	 * @return 상태 응답 객체
	 * @author 함예정
	 * @since 2025-05-11
	 */
	private ArticleCollectorStatusResponse getCollectorStatusResponse(boolean result) {
		return new ArticleCollectorStatusResponse(result);
	}
}
