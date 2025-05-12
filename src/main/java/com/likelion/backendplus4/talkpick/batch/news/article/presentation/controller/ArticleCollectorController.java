package com.likelion.backendplus4.talkpick.batch.news.article.presentation.controller;

import static com.likelion.backendplus4.talkpick.batch.common.response.ApiResponse.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.backendplus4.talkpick.batch.common.response.ApiResponse;
import com.likelion.backendplus4.talkpick.batch.news.article.application.port.in.ArticleCollectorUseCase;
import com.likelion.backendplus4.talkpick.batch.news.article.application.service.dto.ArticleCollectorStatusResponse;

import lombok.RequiredArgsConstructor;

/**
 * 뉴스 수집 스케줄러 컨트롤러.
 * 수집기 실행 및 정지를 위한 API 엔드포인트를 제공한다.
 * 내부적으로 {@link ArticleCollectorUseCase}를 호출하여 작업을 위임한다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/news/collector")
public class ArticleCollectorController {
	private final ArticleCollectorUseCase articleCollectorUsecase;

	/**
	 * 뉴스 RSS 수집을 시작한다.
	 *
	 * @return 수집기 상태 응답 (실행 여부 및 메시지 포함)
	 * @since 2025-05-10
	 * @author 함예정
	 */
	@PostMapping("/start")
	public ResponseEntity<ApiResponse<ArticleCollectorStatusResponse>> start() {
		return success(articleCollectorUsecase.start());
	}

	/**
	 * 뉴스 RSS 수집을 정지한다.
	 *
	 * @return 수집기 상태 응답 (정지 여부 및 메시지 포함)
	 * @since 2025-05-10
	 * @author 함예정
	 */
	@DeleteMapping("/stop")
	public ResponseEntity<ApiResponse<ArticleCollectorStatusResponse>> stop() {
		return success(articleCollectorUsecase.stop());
	}
}
