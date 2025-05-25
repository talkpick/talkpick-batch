package com.likelion.backendplus4.talkpick.batch.news.viewcount.infrastructure.manager;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.repository.NewsInfoJpaRepository;
import com.likelion.backendplus4.talkpick.batch.news.viewcount.infrastructure.item.ViewCountItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 조회수 업데이트 로직을 처리하는 서비스 클래스입니다.
 * 배치 처리를 통해 효율적으로 데이터베이스 접근을 최적화합니다.
 *
 * @since 2025-05-23
 * @author 양병학
 * @modified 2025-05-24 엔티티 변환 로직을 Mapper로 분리
 * @modified 2025-05-25 예외 처리 로직을 구체적인 위치별로 분리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ViewCountBatchManager {

	private final NewsInfoJpaRepository repository;
	private final JdbcTemplate jdbcTemplate;
	private final int batchSize = 100;

	/**
	 * 조회수 데이터를 일괄 업데이트합니다.
	 *
	 * 1. 항목 목록 유효성 검사
	 * 2. 조회수 맵 생성
	 * 3. GUID 목록 추출
	 * 4. 배치 단위로 업데이트 실행
	 *
	 * @param items 업데이트할 조회수 항목 목록
	 * @return 업데이트된 항목 수
	 * @throws ArticleCollectorException 배치 업데이트 실패 시
	 * @since 2025-05-23 최초 작성
	 * @author 양병학
	 */
	@Transactional
	public int updateViewCounts(List<? extends ViewCountItem> items) {
		if (isEmptyItemList(items)) {
			return 0;
		}

		try {
			Map<String, Long> viewCountMap = createViewCountMap(items);
			List<String> allGuids = extractGuidsFromMap(viewCountMap);
			return processBatchUpdates(allGuids, viewCountMap);
		} catch (Exception e) {
			log.error("조회수 데이터 처리 중 오류 발생: {}", e.getMessage(), e);
			throw new ArticleCollectorException(ArticleCollectorErrorCode.VIEW_COUNT_DATA_VALIDATION_FAILED, e);
		}
	}

	/**
	 * 항목 목록이 비어있는지 확인합니다.
	 *
	 * @param items 확인할 항목 목록
	 * @return 비어있으면 true, 아니면 false
	 */
	private boolean isEmptyItemList(List<? extends ViewCountItem> items) {
		return items == null || items.isEmpty();
	}

	/**
	 * 조회수 항목 목록에서 guid를 키로 하고 조회수를 값으로 하는 맵을 생성합니다.
	 *
	 * @param items 조회수 항목 목록
	 * @return 조회수 맵 (guid -> 조회수)
	 */
	private Map<String, Long> createViewCountMap(List<? extends ViewCountItem> items) {
		return items.stream()
			.collect(Collectors.toMap(ViewCountItem::getNewsId, ViewCountItem::getViewCount,
				(existing, replacement) -> replacement));
	}

	/**
	 * 맵에서 모든 guid를 추출합니다.
	 *
	 * @param viewCountMap 조회수 맵
	 * @return guid 목록
	 */
	private List<String> extractGuidsFromMap(Map<String, Long> viewCountMap) {
		return new ArrayList<>(viewCountMap.keySet());
	}

	/**
	 * 전체 배치 업데이트 프로세스를 실행합니다.
	 *
	 * @param allGuids 모든 guid 목록
	 * @param viewCountMap 조회수 맵
	 * @return 총 업데이트된 항목 수
	 */
	private int processBatchUpdates(List<String> allGuids, Map<String, Long> viewCountMap) {
		int totalUpdated = 0;

		for (int i = 0; i < allGuids.size(); i += batchSize) {
			List<String> batchGuids = extractBatchGuids(allGuids, i);
			int batchUpdated = executeBatchUpdate(batchGuids, viewCountMap);

			totalUpdated += batchUpdated;
			logBatchUpdateResult(i, batchGuids.size(), batchUpdated);
		}

		return totalUpdated;
	}

	/**
	 * 전체 guid 목록에서 현재 배치에 해당하는 guid를 추출합니다.
	 *
	 * @param allGuids 모든 guid 목록
	 * @param startIndex 시작 인덱스
	 * @return 현재 배치 guid 목록
	 */
	private List<String> extractBatchGuids(List<String> allGuids, int startIndex) {
		int endIndex = Math.min(startIndex + batchSize, allGuids.size());
		return allGuids.subList(startIndex, endIndex);
	}

	/**
	 * 배치 업데이트 결과를 로깅합니다.
	 *
	 * @param startIndex 시작 인덱스
	 * @param batchSize 배치 크기
	 * @param updatedCount 업데이트된 항목 수
	 */
	private void logBatchUpdateResult(int startIndex, int batchSize, int updatedCount) {
		log.debug("배치 업데이트 완료: {}-{}, {}건 성공", startIndex, startIndex + batchSize - 1, updatedCount);
	}

	/**
	 * 단일 배치에 대한 업데이트를 실행합니다.
	 * JDBC 템플릿을 사용하여 직접 업데이트를 수행합니다.
	 *
	 * @param batchGuids 처리할 guid 목록
	 * @param viewCountMap 조회수 맵 (guid -> 조회수)
	 * @return 업데이트된 항목 수
	 */
	private int executeBatchUpdate(List<String> batchGuids, Map<String, Long> viewCountMap) {
		try {
			List<ViewCountItem> batchItems = createBatchItems(batchGuids, viewCountMap);
			int[] updateCounts = performJdbcBatchUpdate(batchItems);

			int totalUpdated = calculateTotalUpdatedCount(updateCounts);
			logJdbcUpdateResult(totalUpdated, batchItems.size());

			return totalUpdated;
		} catch (DataAccessException e) {
			log.error("데이터베이스 조회수 업데이트 실패 - GUID: {}: {}", batchGuids, e.getMessage(), e);
			throw new ArticleCollectorException(ArticleCollectorErrorCode.VIEW_COUNT_DB_UPDATE_FAILED, e);
		} catch (Exception e) {
			log.error("배치 아이템 생성 실패 - GUID: {}: {}", batchGuids, e.getMessage(), e);
			throw new ArticleCollectorException(ArticleCollectorErrorCode.VIEW_COUNT_BATCH_ITEM_CREATE_FAILED, e);
		}
	}

	/**
	 * 배치 업데이트를 위한 ViewCountItem 목록을 생성합니다.
	 *
	 * @param batchGuids 배치 guid 목록
	 * @param viewCountMap 조회수 맵
	 * @return ViewCountItem 목록
	 */
	private List<ViewCountItem> createBatchItems(List<String> batchGuids, Map<String, Long> viewCountMap) {
		return batchGuids.stream()
			.map(guid -> new ViewCountItem(guid, viewCountMap.get(guid)))
			.collect(Collectors.toList());
	}

	/**
	 * JDBC 배치 업데이트를 실행합니다.
	 *
	 * @param batchItems 배치 항목 목록
	 * @return 업데이트 결과 배열
	 */
	private int[] performJdbcBatchUpdate(List<ViewCountItem> batchItems) {
		try {
			return jdbcTemplate.batchUpdate(
				"UPDATE article SET view_count = ? WHERE guid = ?",
				new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						ViewCountItem item = batchItems.get(i);
						ps.setLong(1, item.getViewCount());
						ps.setString(2, item.getNewsId());
					}

					@Override
					public int getBatchSize() {
						return batchItems.size();
					}
				});
		} catch (DataAccessException e) {
			log.error("JDBC 배치 업데이트 실행 실패: {}", e.getMessage(), e);
			throw new ArticleCollectorException(ArticleCollectorErrorCode.VIEW_COUNT_JDBC_BATCH_FAILED, e);
		}
	}

	/**
	 * 업데이트 결과 배열에서 총 업데이트된 항목 수를 계산합니다.
	 *
	 * @param updateCounts 업데이트 결과 배열
	 * @return 총 업데이트된 항목 수
	 */
	private int calculateTotalUpdatedCount(int[] updateCounts) {
		return Arrays.stream(updateCounts).sum();
	}

	/**
	 * JDBC 업데이트 결과를 로깅합니다.
	 *
	 * @param totalUpdated 총 업데이트된 항목 수
	 * @param totalAttempted 총 시도된 항목 수
	 */
	private void logJdbcUpdateResult(int totalUpdated, int totalAttempted) {
		log.debug("JDBC 배치 업데이트 완료: {}건 성공 / {}건 시도", totalUpdated, totalAttempted);
	}
}