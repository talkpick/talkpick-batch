package com.likelion.backendplus4.talkpick.batch.news.article.application.exception;

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