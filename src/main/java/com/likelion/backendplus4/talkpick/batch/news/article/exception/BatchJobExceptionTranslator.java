package com.likelion.backendplus4.talkpick.batch.news.article.exception;

import java.util.Map;

import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;

/**
 * Spring Batch 작업 실행 중 발생할 수 있는 예외를 <p>
 * {@link ArticleCollectorErrorCode}로 변환하는 컴포넌트입니다.
 */
@Component
public class BatchJobExceptionTranslator {
	private static final Map<Class<? extends Exception>, ArticleCollectorErrorCode> CODE_MAP =
		Map.of(
			JobExecutionAlreadyRunningException.class,    ArticleCollectorErrorCode.JOB_ALREADY_RUNNING,
			JobRestartException.class,                   ArticleCollectorErrorCode.JOB_RESTART_FAIL,
			JobInstanceAlreadyCompleteException.class,   ArticleCollectorErrorCode.JOB_ALREADY_COMPLETE,
			JobParametersInvalidException.class,         ArticleCollectorErrorCode.INVALID_JOB_PARAMETER
		);

	/**
	 * 주어진 예외를 해당하는 {@link ArticleCollectorErrorCode}로 변환합니다.
	 * 정의되지 않은 예외 클래스의 경우 {@code UNKNOWN_ERROR}를 반환합니다.
	 *
	 * @param e 변환할 예외 객체
	 * @return 매핑된 {@link ArticleCollectorErrorCode}
	 */
	public ArticleCollectorErrorCode translate(Exception e) {
		return CODE_MAP.getOrDefault(e.getClass(), ArticleCollectorErrorCode.UNKNOWN_ERROR);
	}
}
