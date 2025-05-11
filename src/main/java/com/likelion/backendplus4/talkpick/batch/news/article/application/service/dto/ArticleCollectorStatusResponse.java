package com.likelion.backendplus4.talkpick.batch.news.article.application.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
/**
 * 실행 상태를 클라이언트에 전달하기 위한 응답 DTO.
 *
 * @author 함예정
 * @since 2025-05-11
 */
@Getter
@AllArgsConstructor
public class ArticleCollectorStatusResponse {
	private final boolean running;
	private final String message;
}
