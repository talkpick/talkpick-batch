package com.likelion.backendplus4.talkpick.batch.news.article.application.port.in;

import com.likelion.backendplus4.talkpick.batch.news.article.application.service.dto.CollectorStatusResponse;

public interface NewsCollectorUseCase {
	CollectorStatusResponse start();

	CollectorStatusResponse stop();
}
