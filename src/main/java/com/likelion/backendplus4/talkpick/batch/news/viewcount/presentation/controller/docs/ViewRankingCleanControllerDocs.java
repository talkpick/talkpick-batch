package com.likelion.backendplus4.talkpick.batch.news.viewcount.presentation.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/**
 * 관리자용 Redis 랭킹 정리 API 문서 정의 인터페이스
 */
@Tag(
	name = "Admin News Cleanup",
	description = "Redis 랭킹 데이터를 수동으로 정리하는 관리자용 API"
)
public interface ViewRankingCleanControllerDocs {

	@Operation(
		summary = "Redis 랭킹 정리 실행",
		description = "Redis에 저장된 3일 이상 된 랭킹 키를 삭제하여 Redis 메모리를 정리합니다."
	)
	ResponseEntity<String> executeManualCleanup();
}