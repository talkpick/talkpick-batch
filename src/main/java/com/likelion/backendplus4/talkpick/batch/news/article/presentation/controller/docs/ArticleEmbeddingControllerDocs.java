package com.likelion.backendplus4.talkpick.batch.news.article.presentation.controller.docs;

import com.likelion.backendplus4.talkpick.batch.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(
	name = "Article Embedding",
	description = "뉴스 기사 임베딩 처리 API"
)
public interface ArticleEmbeddingControllerDocs {

	@Operation(
		summary = "전체 뉴스 임베딩 실행",
		description = "데이터베이스에 저장된 모든 뉴스 기사의 내용을 임베딩 처리합니다."
	)
	ResponseEntity<ApiResponse<Void>> indexAllNews();
}