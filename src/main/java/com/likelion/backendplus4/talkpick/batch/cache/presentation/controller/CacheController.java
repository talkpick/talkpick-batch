package com.likelion.backendplus4.talkpick.batch.cache.presentation.controller;

import static com.likelion.backendplus4.talkpick.batch.common.response.ApiResponse.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.backendplus4.talkpick.batch.cache.application.port.in.CacheUseCase;
import com.likelion.backendplus4.talkpick.batch.cache.presentation.controller.docs.CacheControllerDocs;
import com.likelion.backendplus4.talkpick.batch.common.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cache")
@RequiredArgsConstructor
public class CacheController implements CacheControllerDocs {
	private final CacheUseCase cacheUseCase;

	@Override
	@DeleteMapping("/news/latest")
	public ResponseEntity<ApiResponse<Boolean>> resetCacheByLatestNews(){
		return success(cacheUseCase.clearCacheByLatestNews());
	}
}
