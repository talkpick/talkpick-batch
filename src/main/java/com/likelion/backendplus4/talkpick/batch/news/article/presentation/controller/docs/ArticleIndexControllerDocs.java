package com.likelion.backendplus4.talkpick.batch.news.article.presentation.controller.docs;

import com.likelion.backendplus4.talkpick.batch.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(
	name = "Article Index",
	description = "전체 뉴스 데이터를 색인하는 API"
)
public interface ArticleIndexControllerDocs {

	@Operation(
		summary = "전체 뉴스 색인 실행",
		description = "데이터베이스에 저장된 모든 뉴스 정보를 색인 처리합니다."
	)
	ResponseEntity<ApiResponse<Void>> indexAllNews();
}