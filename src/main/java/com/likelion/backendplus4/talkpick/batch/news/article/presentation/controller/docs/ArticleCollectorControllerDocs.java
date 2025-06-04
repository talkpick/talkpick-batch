package com.likelion.backendplus4.talkpick.batch.news.article.presentation.controller.docs;

import com.likelion.backendplus4.talkpick.batch.common.response.ApiResponse;
import com.likelion.backendplus4.talkpick.batch.news.article.application.service.dto.ArticleCollectorStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(
	name = "Article Collector",
	description = "뉴스 RSS 수집 스케줄러 시작/정지 API"
)
public interface ArticleCollectorControllerDocs {

	@Operation(
		summary = "뉴스 RSS 수집 시작",
		description = "뉴스 RSS 수집기를 작동시켜 주기적으로 RSS 피드를 수집하도록 합니다."
	)
	ResponseEntity<ApiResponse<ArticleCollectorStatusResponse>> start();

	@Operation(
		summary = "뉴스 RSS 수집 정지",
		description = "현재 작동 중인 뉴스 RSS 수집기를 중지합니다."
	)
	ResponseEntity<ApiResponse<ArticleCollectorStatusResponse>> stop();
}