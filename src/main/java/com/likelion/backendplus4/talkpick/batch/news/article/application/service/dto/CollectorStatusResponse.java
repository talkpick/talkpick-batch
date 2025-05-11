package com.likelion.backendplus4.talkpick.batch.news.article.application.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CollectorStatusResponse {
	private final boolean running;
	private final String message;
}
