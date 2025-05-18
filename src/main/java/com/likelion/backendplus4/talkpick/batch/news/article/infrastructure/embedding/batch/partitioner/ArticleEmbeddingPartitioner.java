package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.embedding.batch.partitioner;

import java.util.List;
import java.util.Map;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.embedding.batch.exception.EmbeddingException;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.embedding.batch.exception.error.EmbeddingErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.support.partitioner.IdRangePartitionCalculator;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.support.partitioner.PartitionMapBuilder;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.support.partitioner.dto.ArticleIdRange;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ArticleEmbeddingPartitioner implements Partitioner {
	private static final String QUERY_GET_MIN_ID = """
		SELECT MIN(a.id) FROM ArticleEntity a WHERE a.summary IS NOT NULL AND a.summaryVector IS NULL
		""";
	private static final String QUERY_GET_MAX_ID = """
		SELECT MAX(a.id) FROM ArticleEntity a WHERE a.summary IS NOT NULL AND a.summaryVector IS NULL
		""";

	private final IdRangePartitionCalculator calculator;

	@PersistenceContext
	private final EntityManager entityManager;

	/**
	 * ID 범위를 기준으로 gridSize만큼 파티션을 분할하여 반환한다.
	 *
	 * @param gridSize 생성할 파티션 수
	 * @return 각 파티션의 ExecutionContext를 담은 맵
	 * @author 함예정
	 * @since 2025-05-18
	 */
	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {
		log.info("Partitioning article embedding partitioner with gridSize: {}", gridSize);
		Long minId = createQuery(QUERY_GET_MIN_ID);
		Long maxId = createQuery(QUERY_GET_MAX_ID);

		throwIfInvalidIdRange(minId, maxId);
		List<ArticleIdRange> ranges = calculator.calculate(minId, maxId, gridSize);
		return PartitionMapBuilder.build(ranges);
	}

	/**
	 * 주어진 JPQL 쿼리를 실행하여 Long 타입 결과를 조회한다.
	 *
	 * @param query 실행할 JPQL 쿼리 문자열
	 * @return 쿼리 결과 값
	 * @author 함예정
	 * @since 2025-05-17
	 */
	private Long createQuery(String query) {
		return entityManager.createQuery(
			query,
			Long.class
		).getSingleResult();
	}

	/**
	 * 조회된 ID 범위가 유효하지 않을 경우 예외를 발생시킨다.
	 *
	 * minId 또는 maxId가 null이거나, minId가 maxId보다 큰 경우
	 * {@link EmbeddingException}을 {@link EmbeddingErrorCode#ITEM_NOT_FOUND}와 함께 발생시킨다.
	 *
	 * @param minId ID 범위의 최소값
	 * @param maxId ID 범위의 최대값
	 * @throws EmbeddingException 유효하지 않은 ID 범위일 경우
	 * @author 함예정
	 * @since 2025-05-18
	 */
	private void throwIfInvalidIdRange(Long minId, Long maxId) {
		if (minId == null || maxId == null || minId > maxId) {
			throw new EmbeddingException(EmbeddingErrorCode.ITEM_NOT_FOUND);
		}
	}
}