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
	/**
	 * 스케줄러 상태와 메시지를 기반으로 응답 객체를 생성한다.
	 *
	 * @param status 실행 여부 (true: 실행 중, false: 정지됨)
	 * @param message 상태에 대한 설명 메시지
	 * @return 상태 정보가 포함된 CollectorStatusResponse 객체
	 * @author 함예정
	 * @since 2025-05-11
	 */
	public static CollectorStatusResponse toResponse(boolean status, String message){
		return new CollectorStatusResponse(status, message);
	}
}
