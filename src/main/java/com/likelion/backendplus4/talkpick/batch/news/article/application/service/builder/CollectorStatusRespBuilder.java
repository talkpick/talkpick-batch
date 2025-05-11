package com.likelion.backendplus4.talkpick.batch.news.article.application.service.builder;

import com.likelion.backendplus4.talkpick.batch.news.article.application.service.dto.CollectorStatusResponse;

/**
 * 응답 객체 빌더 클래스 <p>
 * boolean 상태값과 메시지를 기반으로 {@link CollectorStatusResponse} 객체를 생성한다.
 *
 * @author 함예정
 * @since 2025-05-11
 */
public class CollectorStatusRespBuilder {
	public static CollectorStatusResponse toResponse(boolean status, String message){
		return new CollectorStatusResponse(status, message);
	}
}
