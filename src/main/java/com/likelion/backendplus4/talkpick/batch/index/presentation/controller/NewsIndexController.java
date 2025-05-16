package com.likelion.backendplus4.talkpick.batch.index.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.backendplus4.talkpick.batch.common.annotation.logging.EntryExitLog;
import com.likelion.backendplus4.talkpick.batch.common.annotation.logging.TimeTracker;
import com.likelion.backendplus4.talkpick.batch.common.response.ApiResponse;
import com.likelion.backendplus4.talkpick.batch.index.application.port.in.NewsIndexUseCase;

import lombok.RequiredArgsConstructor;

/**
 * 뉴스 데이터 일괄 색인을 위한 REST 컨트롤러
 *
 * @since 2025-05-15
 */
@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
public class NewsIndexController {

	private final NewsIndexUseCase indexUseCase;

	/**
	 * 전체 뉴스 정보를 색인하고 처리 건수를 반환한다.
	 *
	 * @return ApiResponse에 래핑된 색인된 뉴스 건수
	 * @author 정안식
	 * @since 2025-05-15
	 */
	@EntryExitLog
	@TimeTracker
	@PostMapping("/index")
	public ResponseEntity<ApiResponse<Integer>> indexAllNews() {
		int count = indexUseCase.indexAllNewsInfo();
		return ApiResponse.success(count);
	}
}
