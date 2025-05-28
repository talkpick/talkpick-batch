package com.likelion.backendplus4.talkpick.batch.news.article.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.backendplus4.talkpick.batch.common.response.ApiResponse;
import com.likelion.backendplus4.talkpick.batch.news.article.application.port.out.NewsEmbedUseCase;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/news")
public class ArticleEmbeddingController {
	private final NewsEmbedUseCase newsEmbedUseCase;

	@PostMapping("/embed")
	public ResponseEntity<ApiResponse<Void>> indexAllNews() {
		newsEmbedUseCase.embedAllNewsInfo();
		return ApiResponse.success();
	}
}
