package com.likelion.backendplus4.talkpick.batch.news.article.application.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 실행 상태를 클라이언트에 전달하기 위한 응답 DTO.
 *
 * @author 함예정
 * @since 2025-05-11
 */
@Getter
public class ArticleCollectorStatusResponse {
	private final boolean running;
	private final String message;

	private final String failMessage = "처리에 실패했습니다";
	private final String successMessage = "요청이 성공적으로 전달 됐습니다";


	public ArticleCollectorStatusResponse(boolean running) {
		this.running = running;
		this.message = running ? successMessage : failMessage;
	}
}
