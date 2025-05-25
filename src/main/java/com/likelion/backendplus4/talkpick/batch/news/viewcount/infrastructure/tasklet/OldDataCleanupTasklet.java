package com.likelion.backendplus4.talkpick.batch.news.viewcount.infrastructure.tasklet;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.data.redis.core.RedisTemplate;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.ViewCountInvalidFormatException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;

/**
 * 오래된 조회수 데이터를 정리하는 Tasklet 구현 클래스입니다.
 * 정리 정책에 따라 Redis의 오래된 데이터를 삭제합니다.
 *
 * @since 2025-05-20
 */
public class OldDataCleanupTasklet implements Tasklet {

	private static final String VIEW_COUNT_PATTERN = "news:viewCount:*";
	private static final int DATA_RETENTION_DAYS = 30;
	private static final int RECENT_DATA_DAYS = 7;
	private static final long MIN_VIEW_COUNT_THRESHOLD = 1000L;
	private static final String PREVIOUS_STEP_NAME = "viewCountSyncStep";

	private final RedisTemplate<String, String> redisTemplate;

	/**
	 * Redis 템플릿을 받아 Tasklet을 초기화합니다.
	 *
	 * @param redisTemplate Redis 작업을 위한 템플릿
	 */
	public OldDataCleanupTasklet(RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	/**
	 * Tasklet 실행 메서드입니다.
	 * Redis의 조회수 데이터를 정리 정책에 따라 관리합니다.
	 *
	 * 1. 이전 Step 완료 확인
	 * 2. 조회수 키 목록 조회
	 * 3. 키별 정리 정책 적용
	 *
	 * @param contribution Step 실행 정보
	 * @param chunkContext Chunk 실행 컨텍스트
	 * @return 반복 상태 (FINISHED: 작업 완료)
	 * @modified 2025-05-21 양병학
	 *  - 예외 처리 로직 개선
	 */
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		return executeInternal(contribution, chunkContext);
	}

	private RepeatStatus executeInternal(StepContribution contribution, ChunkContext chunkContext) {
		try {
			return performCleanup(contribution, chunkContext);
		} catch (Exception e) {
			handleCleanupException(e);
			return RepeatStatus.FINISHED;
		}
	}

	/**
	 * 데이터 정리 작업을 수행합니다.
	 *
	 * 1. 이전 Step 완료 확인
	 * 2. Redis 키 목록 조회
	 * 3. 정리 정책 적용하여 데이터 삭제
	 *
	 * @param contribution Step 실행 정보
	 * @param chunkContext Chunk 실행 컨텍스트
	 * @return 작업 완료 상태
	 * @since 2025-05-25
	 * @author 양병학
	 */
	private RepeatStatus performCleanup(StepContribution contribution, ChunkContext chunkContext) {
		if (!isPreviousStepCompleted(chunkContext)) {
			return RepeatStatus.FINISHED;
		}

		Set<String> keys = fetchViewCountKeys();
		if (isEmptyKeys(keys)) {
			return RepeatStatus.FINISHED;
		}

		processAllKeys(keys);
		return RepeatStatus.FINISHED;
	}

	/**
	 * 이전 Step이 성공적으로 완료되었는지 확인합니다.
	 *
	 * @param chunkContext 현재 Chunk 컨텍스트
	 * @return 이전 Step이 완료되었으면 true, 아니면 false
	 */
	private boolean isPreviousStepCompleted(ChunkContext chunkContext) {
		StepExecution previousStepExecution = findStepExecution(chunkContext, PREVIOUS_STEP_NAME);
		return previousStepExecution != null &&
			ExitStatus.COMPLETED.equals(previousStepExecution.getExitStatus());
	}

	/**
	 * 조회수 키 패턴에 일치하는 모든 키를 가져옵니다.
	 *
	 * @return 조회수 키 집합
	 */
	private Set<String> fetchViewCountKeys() {
		return redisTemplate.keys(VIEW_COUNT_PATTERN);
	}

	/**
	 * 키 집합이 비어있는지 확인합니다.
	 *
	 * @param keys 키 집합
	 * @return 비어있으면 true, 아니면 false
	 */
	private boolean isEmptyKeys(Set<String> keys) {
		return keys == null || keys.isEmpty();
	}

	/**
	 * 모든 키를 처리합니다.
	 *
	 * @param keys 처리할 키 집합
	 */
	private void processAllKeys(Set<String> keys) {
		for (String key : keys) {
			processKey(key);
		}
	}

	/**
	 * 개별 키에 대한 정리 처리를 수행합니다.
	 *
	 * @param key 처리할 Redis 키
	 */
	private void processKey(String key) {
		Long ttl = getKeyTtl(key);
		if (!isValidTtl(ttl)) {
			return;
		}

		String countValue = getCountValue(key);
		if (null == countValue) {
			return;
		}

		Long viewCount = parseViewCount(countValue, key);
		applyCleanupPolicy(key, ttl, viewCount);
	}

	/**
	 * 키의 TTL(Time To Live)을 가져옵니다.
	 *
	 * @param key Redis 키
	 * @return TTL 값(일 단위)
	 */
	private Long getKeyTtl(String key) {
		return redisTemplate.getExpire(key, TimeUnit.DAYS);
	}

	/**
	 * TTL 값이 유효한지 확인합니다.
	 *
	 * @param ttl TTL 값
	 * @return 유효하면 true, 아니면 false
	 */
	private boolean isValidTtl(Long ttl) {
		return null != ttl && 0 <= ttl;
	}

	/**
	 * 키에 해당하는 조회수 값을 가져옵니다.
	 *
	 * @param key Redis 키
	 * @return 조회수 값 문자열
	 */
	private String getCountValue(String key) {
		return redisTemplate.opsForValue().get(key);
	}

	/**
	 * 문자열 값을 조회수(Long)로 파싱합니다.
	 *
	 * @param countValue 파싱할 문자열 값
	 * @param key 관련 Redis 키 (로깅 및 오류 메시지용)
	 * @return 파싱된 조회수 값
	 * @throws ViewCountInvalidFormatException 파싱 실패 시
	 */
	private Long parseViewCount(String countValue, String key) {
		try {
			return Long.parseLong(countValue);
		} catch (NumberFormatException e) {
			throw new ViewCountInvalidFormatException(
				"Redis 키 '" + key + "'의 값이 유효한 숫자 형식이 아닙니다: " + countValue, e);
		}
	}

	/**
	 * 정리 정책을 적용합니다.
	 *
	 * 1. 보존 기간 계산
	 * 2. 최대 보존 기간 초과 시 삭제
	 * 3. 최근 데이터가 아니면서 조회수가 낮은 경우 삭제
	 *
	 * @param key Redis 키
	 * @param ttl TTL 값(일 단위)
	 * @param viewCount 조회수 값
	 * @since 2025-05-20 최초 작성
	 * @author 양병학
	 */
	private void applyCleanupPolicy(String key, Long ttl, Long viewCount) {
		Long remainingDays = DATA_RETENTION_DAYS - ttl;

		if (remainingDays >= DATA_RETENTION_DAYS) {
			deleteKey(key);
		}

		if (remainingDays >= RECENT_DATA_DAYS && viewCount < MIN_VIEW_COUNT_THRESHOLD) {
			deleteKey(key);
		}
	}

	/**
	 * Redis에서 키를 삭제합니다.
	 *
	 * @param key 삭제할 Redis 키
	 */
	private void deleteKey(String key) {
		redisTemplate.delete(key);
	}

	/**
	 * 정리 작업 예외를 처리합니다.
	 *
	 * @param e 발생한 예외
	 * @throws ArticleCollectorException 변환된 도메인 예외
	 */
	private void handleCleanupException(Exception e) {
		throw new ArticleCollectorException(ArticleCollectorErrorCode.VIEW_COUNT_CLEANUP_FAILED, e);
	}

	/**
	 * 지정된 이름의 Step 실행 정보를 찾습니다.
	 *
	 * @param chunkContext 현재 Chunk 컨텍스트
	 * @param stepName 찾을 Step 이름
	 * @return 해당 Step의 실행 정보 또는 null
	 */
	private StepExecution findStepExecution(ChunkContext chunkContext, String stepName) {
		for (StepExecution execution : chunkContext.getStepContext()
			.getStepExecution()
			.getJobExecution()
			.getStepExecutions()) {
			if (stepName.equals(execution.getStepName())) {
				return execution;
			}
		}
		return null;
	}
}