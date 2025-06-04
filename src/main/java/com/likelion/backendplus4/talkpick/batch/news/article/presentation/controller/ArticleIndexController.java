package com.likelion.backendplus4.talkpick.batch.news.article.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.backendplus4.talkpick.batch.common.annotation.logging.EntryExitLog;
import com.likelion.backendplus4.talkpick.batch.common.annotation.logging.TimeTracker;
import com.likelion.backendplus4.talkpick.batch.common.response.ApiResponse;
import com.likelion.backendplus4.talkpick.batch.news.article.application.port.in.NewsIndexUseCase;
import com.likelion.backendplus4.talkpick.batch.news.article.presentation.controller.docs.ArticleIndexControllerDocs;

import lombok.RequiredArgsConstructor;

/**
 * 뉴스 데이터 일괄 색인을 위한 REST 컨트롤러
 *
 * @since 2025-05-15
 * @modified 2025-05-29
 */
@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
public class ArticleIndexController implements ArticleIndexControllerDocs {

	private final NewsIndexUseCase indexUseCase;

	/**
	 * 전체 뉴스 정보를 색인하고 처리 건수를 반환한다.
	 *
	 * @return ApiResponse에 래핑된 색인된 뉴스 건수
	 * @author 정안식
	 * @since 2025-05-15
	 * @modified 2025-05-29 함예정
	 * 25-05-29 - 쿼츠 전환으로 저장된 개수 반환 불가에 따른 반환 타입 수정 (int -> void)
	 */
	@EntryExitLog
	@TimeTracker
	@Override
	@PostMapping("/index")
	public ResponseEntity<ApiResponse<Void>> indexAllNews() {
		indexUseCase.indexAllNewsInfo();
		return ApiResponse.success();
	}
}
