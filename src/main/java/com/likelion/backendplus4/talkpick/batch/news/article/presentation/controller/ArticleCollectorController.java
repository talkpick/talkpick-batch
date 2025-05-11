package com.likelion.backendplus4.talkpick.batch.news.article.presentation.controller;

import static com.likelion.backendplus4.talkpick.batch.common.response.ApiResponse.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.backendplus4.talkpick.batch.common.response.ApiResponse;
import com.likelion.backendplus4.talkpick.batch.news.article.application.port.in.NewsCollectorUseCase;
import com.likelion.backendplus4.talkpick.batch.news.article.application.service.dto.CollectorStatusResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/news/collector")
public class ArticleCollectorController {
	private final NewsCollectorUseCase newsCollectorUsecase;


	@PostMapping("/start")
	public ResponseEntity<ApiResponse<CollectorStatusResponse>> start() {
		return success(newsCollectorUsecase.start());
	}

	@DeleteMapping("/stop")
	public ResponseEntity<ApiResponse<CollectorStatusResponse>> stop() {
		return success(newsCollectorUsecase.stop());
	}
}
