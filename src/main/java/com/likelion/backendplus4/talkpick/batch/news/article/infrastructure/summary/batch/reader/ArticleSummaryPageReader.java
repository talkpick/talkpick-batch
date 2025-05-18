package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.summary.batch.reader;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;

import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * 요약되지 않은 뉴스 기사 데이터를 ID 범위 기반으로 페이징 조회하는 JPA ItemReader.
 * 파티셔닝된 슬레이브 Step에서 각 파티션이 담당할 ID 구간의 데이터를 읽기 위해 사용된다.
 *
 * @since 2025-05-17
 */
@Component
@Slf4j
@StepScope
public class ArticleSummaryPageReader extends JpaPagingItemReader<ArticleEntity> {
	private static final String JPQL = """
		SELECT a
			FROM ArticleEntity a
		WHERE a.summary IS NULL
			AND a.id BETWEEN :minId AND :maxId
		""";

	/**
	 * 지정된 ID 범위에 해당하는 기사 데이터를 페이징 방식으로 읽어오는 Reader를 초기화한다.
	 *
	 * @param entityManagerFactory JPA EntityManagerFactory
	 * @param minId 파티션에서 처리할 최소 ID (StepExecutionContext에서 주입됨)
	 * @param maxId 파티션에서 처리할 최대 ID (StepExecutionContext에서 주입됨)
	 * @author 함예정
	 * @since 2025-05-17
	 */
	public ArticleSummaryPageReader(
		EntityManagerFactory entityManagerFactory,
		@Value("#{stepExecutionContext[minId]}") Long minId,
		@Value("#{stepExecutionContext[maxId]}") Long maxId) {

		this.setName("articleSummaryReader-" + minId + "-" + maxId);
		this.setEntityManagerFactory(entityManagerFactory);
		this.setQueryString(JPQL);
		Map<String, Object> params = new HashMap<>();
		params.put("minId", minId);
		params.put("maxId", maxId);
		this.setParameterValues(params);
		this.setPageSize(100);
		this.setSaveState(false);
	}
}
