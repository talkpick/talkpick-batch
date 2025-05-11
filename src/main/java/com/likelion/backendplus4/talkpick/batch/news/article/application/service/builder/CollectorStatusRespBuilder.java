package com.likelion.backendplus4.talkpick.batch.news.article.application.service.builder;

import com.likelion.backendplus4.talkpick.batch.news.article.application.service.dto.CollectorStatusResponse;

public class CollectorStatusRespBuilder {
	public static CollectorStatusResponse toResponse(boolean status, String message){
		return new CollectorStatusResponse(status, message);
	}
}
