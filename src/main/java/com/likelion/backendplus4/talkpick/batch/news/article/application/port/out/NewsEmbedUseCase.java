package com.likelion.backendplus4.talkpick.batch.news.article.application.port.out;

import org.springframework.stereotype.Component;

@Component
public interface NewsEmbedUseCase {
	void embedAllNewsInfo();
}
