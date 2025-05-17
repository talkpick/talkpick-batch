package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.summary.batch.processor;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 뉴스 기사를 AI 모델을 통해 요약 처리하는 ItemProcessor 구현체.
 * 입력으로 받은 ArticleEntity의 본문을 요약하여 summary 필드에 설정한 후 반환한다.
 *
 * @since 2025-05-17
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleSummaryProcessor implements ItemProcessor<ArticleEntity, ArticleEntity> {
	private final String prompt = "Summarize the following news in 3–5 concise sentences, objectively, in Korean.\n\n news: \n";
	private final ChatClient chatClient;

	/**
	 * 기사 내용을 AI를 통해 요약하고, 요약 결과를 ArticleEntity에 설정하여 반환한다.
	 *
	 * @param item 요약할 뉴스 기사 엔티티
	 * @return 요약이 포함된 뉴스 기사 엔티티
	 * @author 함예정
	 * @since 2025-05-17
	 */
	@Override
	public ArticleEntity process(ArticleEntity item) {
		log.info("뉴스 요약: id = {}, guid = {}", item.getId(), item.getGuid());
		String newsContent = item.getDescription();
		String summary = getSummary(newsContent);
		item.setSummary(summary);
		return item;
	}
	/**
	 * 주어진 뉴스 기사 본문을 AI 모델을 통해 요약한다.
	 *
	 * @param text 뉴스 기사 본문
	 * @return 요약된 텍스트
	 * @author 함예정
	 * @since 2025-05-17
	 */
	private String getSummary(String text) {
		return chatClient.prompt().user(prompt + text).call().content();
	}
}
