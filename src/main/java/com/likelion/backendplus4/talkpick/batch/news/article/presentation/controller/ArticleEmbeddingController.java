package com.likelion.backendplus4.talkpick.batch.news.article.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.backendplus4.talkpick.batch.common.response.ApiResponse;
import com.likelion.backendplus4.talkpick.batch.news.article.application.port.out.NewsEmbedUseCase;

import lombok.RequiredArgsConstructor;

/**
 * 뉴스 기사 정보를 임베딩하는 작업을 수행하는 REST 컨트롤러입니다.
 *
 * 이 컨트롤러는 클라이언트로부터 요청을 받아 NewsEmbedUseCase를 통해 <p>
 * 데이터베이스에 저장된 모든 뉴스 기사의 내용을 임베딩 처리합니다. <p>
 *
 * @since 2025-05-29
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/news")
public class ArticleEmbeddingController {
	private final NewsEmbedUseCase newsEmbedUseCase;

	/**
	 * 전체 뉴스 정보를 임베딩하는 API
	 *
	 * @return 성공 여부를 담은 ApiResponse
	 * @author 함예정
	 * @since 2025-05-29
	 */
	@PostMapping("/embed")
	public ResponseEntity<ApiResponse<Void>> indexAllNews() {
		newsEmbedUseCase.embedAllNewsInfo();
		return ApiResponse.success();
	}
}
