package com.likelion.backendplus4.talkpick.batch.news.article.application.service;

import org.springframework.stereotype.Service;

import com.likelion.backendplus4.talkpick.batch.news.article.application.port.in.NewsCollectorUseCase;
import com.likelion.backendplus4.talkpick.batch.news.article.application.port.out.CollectorPort;
import com.likelion.backendplus4.talkpick.batch.news.article.application.service.builder.CollectorStatusRespBuilder;
import com.likelion.backendplus4.talkpick.batch.news.article.application.service.dto.CollectorStatusResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsCollectorService implements NewsCollectorUseCase {
	private final String failMessage = "처리에 실패했습니다";
	private final CollectorPort collectorPort;
	
	@Override
	public CollectorStatusResponse start(){
		boolean result = collectorPort.start();
		return getCollectorStatusResponse(result, "스케줄 작업이 시작되었습니다.");
	}



	@Override
	public CollectorStatusResponse stop(){
		boolean result = collectorPort.stop();
		return getCollectorStatusResponse(result, "스케줄 작업이 중단되었습니다.");
	}

	private CollectorStatusResponse getCollectorStatusResponse(boolean result, String successMessage) {
		if(result) {
			return CollectorStatusRespBuilder.toResponse(result, successMessage);
		}
		return CollectorStatusRespBuilder.toResponse(result, failMessage);
	}
}
