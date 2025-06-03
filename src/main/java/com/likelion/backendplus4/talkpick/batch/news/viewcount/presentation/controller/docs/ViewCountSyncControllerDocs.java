package com.likelion.backendplus4.talkpick.batch.news.viewcount.presentation.controller.docs;

import com.likelion.backendplus4.talkpick.batch.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(
	name = "View Count Sync",
	description = "Redis 조회수 데이터를 DB에 동기화하는 API"
)
public interface ViewCountSyncControllerDocs {

	@Operation(
		summary = "조회수 동기화 실행",
		description = "Redis에 저장된 조회수 데이터를 수동으로 DB에 동기화합니다."
	)
	ResponseEntity<ApiResponse<String>> syncViewCount();
}