package com.likelion.backendplus4.talkpick.batch.news.article.exception;

import org.springframework.http.HttpStatus;

import com.likelion.backendplus4.talkpick.batch.common.exception.error.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * RSS 관련 오류 코드를 정의하는 열거형
 *
 * @since 2025-05-10
 * @modified 2025-05-11
 *   - 클래스 주석에서 저자 삭제
 *   - 예외 전역 처리를 위해 상속 구조로 변경
 *   - 예외 전역 처리로 알 수 없는 오류 코드 삭제
 */
@Getter
@RequiredArgsConstructor
public enum ArticleCollectorErrorCode implements ErrorCode {

    // 실행 오류
    JOB_ALREADY_RUNNING(HttpStatus.BAD_REQUEST, 150001, "[Quartz] Batch 실행 실패 - 현재 Job이 이미 실행 중입니다."),
    JOB_ALREADY_COMPLETE(HttpStatus.BAD_REQUEST, 150002, "[Quartz] Batch 실행 실패 - 동일한 JobParameters로 실행된 Job이 이미 완료되었습니다."),
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 550001, "[Quartz] Batch 실행 중 알 수 없는 예외 발생"),
    JOB_RESTART_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, 550002, "[Quartz] Batch 실행 실패 - Job을 재시작할 수 없습니다. 이전 실행 상태가 불안정하거나 종료되지 않았을 수 있습니다."),
    INVALID_JOB_PARAMETER(HttpStatus.INTERNAL_SERVER_ERROR, 550003, "[Quartz] Batch 실행 실패 - JobParameters가 유효하지 않습니다. 필수 파라미터 누락 또는 형식 오류일 수 있습니다."),



    // RSS 정보 로드 관련 오류
    FEED_CONNECTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 450001,"RSS 피드 연결 중 오류가 발생했습니다."),
    FEED_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 450002,"RSS 피드 파싱 중 오류가 발생했습니다."),
    FEED_TIMEOUT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 450003, "RSS 피드 로드 중 시간 초과가 발생했습니다."),

    // Mapper 관련 오류
    MAPPER_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, 450004, "요청한 매퍼를 찾을 수 없습니다."),
    ITEM_MAPPING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 450005,"RSS 항목 매핑 중 오류가 발생했습니다."),

    // 데이터베이스 관련 오류
    DB_SAVE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 530001, "RSS 뉴스를 저장하는 중 오류가 발생했습니다."),
    DUPLICATE_LINK_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 450006,  "이미 존재하는 링크입니다.");

    private final HttpStatus status;
    private final int code;
    private final String message;
    
    @Override
    public HttpStatus httpStatus() {
        return status;
    }

    @Override
    public int codeNumber() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}