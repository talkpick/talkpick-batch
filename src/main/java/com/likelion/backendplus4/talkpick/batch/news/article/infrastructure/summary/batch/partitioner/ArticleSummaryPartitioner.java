package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.summary.batch.partitioner;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

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
@Component
@Slf4j
public class ArticleSummaryPartitioner implements Partitioner {
	private static final String QUERY_GET_MIN_ID = """
		SELECT MIN(a.id) FROM ArticleEntity a WHERE a.summary IS NULL
		""";
	private static final String QUERY_GET_MAX_ID = """
		SELECT MAX(a.id) FROM ArticleEntity a WHERE a.summary IS NULL
		""";

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

		if (isInvalidIdRange(minId, maxId)) {
			log.info("요약할 뉴스가 없습니다");
			return new LinkedHashMap<>();
		}

		return partitionByIdRange(gridSize, maxId, minId);
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

	/**
	 * ID 범위를 기준으로 파티션을 생성한다.
	 *
	 * @param gridSize 파티션 개수
	 * @param maxId 최대 ID 값
	 * @param minId 최소 ID 값
	 * @return 파티션 이름과 ExecutionContext 매핑
	 * @author 함예정
	 * @since 2025-05-17
	 */
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