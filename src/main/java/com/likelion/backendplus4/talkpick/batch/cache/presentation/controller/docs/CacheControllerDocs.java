package com.likelion.backendplus4.talkpick.batch.cache.presentation.controller.docs;

import com.likelion.backendplus4.talkpick.batch.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(
	name = "Cache",
	description = "캐시 초기화 관련 API"
)
public interface CacheControllerDocs {

	@Operation(
		summary = "최신 뉴스 캐시 초기화",
		description = "최신 뉴스 캐시를 삭제하여, 다음 요청 시 새로운 데이터를 조회하도록 합니다."
	)
	ResponseEntity<ApiResponse<Boolean>> resetCacheByLatestNews();
}