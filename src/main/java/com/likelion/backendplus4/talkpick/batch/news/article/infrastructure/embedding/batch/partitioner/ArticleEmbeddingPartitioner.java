package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.embedding.batch.partitioner;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.embedding.batch.exception.EmbeddingException;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.embedding.batch.exception.error.EmbeddingErrorCode;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link Partitioner} 구현체로, 전체 페이지 수(totalPages)를 기준으로
 * 병렬 처리할 수 있도록 페이지 범위를 gridSize만큼 균등하게 분할한다.
 *
 * <p>Spring Batch에서 멀티스레드로 작업을 병렬 처리할 때 사용되며,
 * 각 ExecutionContext에는 'startPage'와 'endPage'가 설정된다.
 *
 * @since 2025-05-17
 */
@Slf4j
@Component
public class ArticleEmbeddingPartitioner implements Partitioner {
	private static final String QUERY_GET_MIN_ID = """
		SELECT MIN(a.id) FROM ArticleEntity a WHERE a.summary IS NOT NULL AND a.summaryVector IS NULL
		""";
	private static final String QUERY_GET_MAX_ID = """
		SELECT MAX(a.id) FROM ArticleEntity a WHERE a.summary IS NOT NULL AND a.summaryVector IS NULL
		""";

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {
		log.info("Partitioning article embedding partitioner with gridSize: {}", gridSize);
		Long minId = createQuery(QUERY_GET_MIN_ID);
		Long maxId = createQuery(QUERY_GET_MAX_ID);

		if (isInvalidIdRange(minId, maxId)) {
			throw new EmbeddingException(EmbeddingErrorCode.API_CALL_ERROR);
		}

		return partitionByIdRange(gridSize, maxId, minId);
	}

	private Long createQuery(String query) {
		return entityManager.createQuery(
			query,
			Long.class
		).getSingleResult();
	}

	private boolean isInvalidIdRange(Long minId, Long maxId) {
		return minId == null || maxId == null || minId > maxId;
	}

	private Map<String, ExecutionContext> partitionByIdRange(int gridSize, Long maxId, Long minId) {
		Map<String, ExecutionContext> partitions = new LinkedHashMap<>();
		long targetSize = ((maxId - minId) + 1) / gridSize;
		long start = minId;
		long end;

		for (int i = 0; i < gridSize; i++) {
			ExecutionContext context = new ExecutionContext();
			end = (i == gridSize - 1) ? maxId : start + targetSize - 1;

			context.putLong("minId", start);
			context.putLong("maxId", end);

			partitions.put("partition" + i, context);
			start = end + 1;
		}
		return partitions;
	}
}