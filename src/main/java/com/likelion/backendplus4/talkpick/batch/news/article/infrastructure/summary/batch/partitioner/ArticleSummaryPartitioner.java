package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.summary.batch.partitioner;

import java.util.List;
import java.util.Map;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.embedding.batch.exception.EmbeddingException;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.embedding.batch.exception.error.EmbeddingErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.summary.batch.exception.ArticleSummaryException;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.summary.batch.exception.error.ArticleSummaryErrorCode;
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
@Component
@Slf4j
@RequiredArgsConstructor
public class ArticleSummaryPartitioner implements Partitioner {
	private static final String QUERY_GET_MIN_ID = """
		SELECT MIN(a.id) FROM ArticleEntity a WHERE a.summary IS NULL
		""";
	private static final String QUERY_GET_MAX_ID = """
		SELECT MAX(a.id) FROM ArticleEntity a WHERE a.summary IS NULL
		""";

	private final IdRangePartitionCalculator calculator;
	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * ID 범위를 기준으로 데이터를 분할한다.
	 * Spring Batch에서 마스터 Step이 병렬로 슬레이브 Step을 실행할 수 있도록 파티션을 생성한다.
	 *
	 * @author 함예정
	 * @since 2025-05-17
	 */
	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {
		Long minId = createQuery(QUERY_GET_MIN_ID);
		Long maxId = createQuery(QUERY_GET_MAX_ID);

		throwIfInvalidIdRange(minId, maxId);
		List<ArticleIdRange> ranges = calculator.calculate(minId, maxId, gridSize);
		return PartitionMapBuilder.build(ranges);
	}

	/**
	 * 주어진 ID 범위가 유효하지 않을 경우 예외를 발생시킨다.
	 *
	 * minId 또는 maxId가 null이거나, minId가 maxId보다 큰 경우
	 * {@link ArticleSummaryException}을 {@link ArticleSummaryErrorCode#ITEM_NOT_FOUND}와 함께 발생시킨다.
	 *
	 * @param minId ID 범위의 최소값
	 * @param maxId ID 범위의 최대값
	 * @throws ArticleSummaryException 유효하지 않은 ID 범위일 경우
	 *
	 * @author 함예정
	 * @since 2025-05-18
	 */
	private void throwIfInvalidIdRange(Long minId, Long maxId) {
		if (minId == null || maxId == null || minId > maxId) {
			throw new ArticleSummaryException(ArticleSummaryErrorCode.ITEM_NOT_FOUND);
		}
	}

	/**
	 * 주어진 JPQL 쿼리를 실행하여 단일 Long 값을 반환한다.
	 *
	 * @param query 실행할 JPQL 쿼리 문자열
	 * @return 조회된 Long 값
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
	 * ID 범위가 유효한지 확인한다.
	 *
	 * @param minId 조회된 최소 ID
	 * @param maxId 조회된 최대 ID
	 * @return 범위가 유효하지 않으면 true 반환
	 * @author 함예정
	 * @since 2025-05-17
	 */
	private boolean isInvalidIdRange(Long minId, Long maxId) {
		return minId == null || maxId == null || minId > maxId;
	}

}