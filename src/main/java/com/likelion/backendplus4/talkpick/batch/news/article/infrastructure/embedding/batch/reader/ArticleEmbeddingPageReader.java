package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.embedding.batch.reader;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;

import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;

@Component
@StepScope
@Slf4j
public class ArticleEmbeddingPageReader extends JpaPagingItemReader<ArticleEntity> {
	private static final String JPQL = """
		SELECT a
			FROM ArticleEntity a
		WHERE a.summaryVector IS NULL
			AND a.summary     IS NOT NULL
			AND a.id BETWEEN :minId AND :maxId
		""";
	public ArticleEmbeddingPageReader(
		EntityManagerFactory entityManagerFactory,
		@Value("#{stepExecutionContext[minId]}") Long minId,
		@Value("#{stepExecutionContext[maxId]}") Long maxId) {

		this.setName("articleEmbeddingReader-" + minId + "-" + maxId);
		this.setEntityManagerFactory(entityManagerFactory);
		this.setQueryString(JPQL);
		Map<String, Object> params = new HashMap<>();
		params.put("minId", minId);
		params.put("maxId", maxId);
		this.setParameterValues(params);
		this.setSaveState(false);
		log.info("Initialized reader for ID range {} ~ {}", minId, maxId);
	}
}
