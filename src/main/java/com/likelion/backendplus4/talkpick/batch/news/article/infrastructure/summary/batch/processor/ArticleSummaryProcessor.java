package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.summary.batch.processor;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.common.annotation.logging.EntryExitLog;
import com.likelion.backendplus4.talkpick.batch.common.annotation.logging.LogMethodValues;
import com.likelion.backendplus4.talkpick.batch.common.annotation.logging.TimeTracker;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ArticleSummaryProcessor implements ItemProcessor<ArticleEntity, ArticleEntity> {
	private final String prompt = "Summarize the following news in 3–5 concise sentences, objectively, in Korean.\n\n news: \n";
	private final ChatClient chatClient;

	@Override
	public ArticleEntity process(ArticleEntity item) {
		String newsContent = item.getDescription();
		String summary = getSummary(newsContent);
		item.setSummary(summary);
		return item;
	}

	private String getSummary(String text) {
		return chatClient
			.prompt()
			.user(prompt + text)
			.call()
			.content();
	}
}
