package com.likelion.backendplus4.talkpick.batch.news.infrastructure.rss.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * RSS 관련 오류 코드를 정의하는 열거형
 *
 * @author 양병학
 * @since 2025-05-10
 */
@Getter
@RequiredArgsConstructor
public enum RssErrorCode {

    // 일반 오류
    UNKNOWN_ERROR("RSS-E001", "알 수 없는 오류가 발생했습니다."),

    // RSS 정보 로드 관련 오류
    FEED_CONNECTION_ERROR("RSS-E101", "RSS 피드 연결 중 오류가 발생했습니다."),
    FEED_PARSING_ERROR("RSS-E102", "RSS 피드 파싱 중 오류가 발생했습니다."),
    FEED_TIMEOUT_ERROR("RSS-E103", "RSS 피드 로드 중 시간 초과가 발생했습니다."),

    // Mapper 관련 오류
    MAPPER_NOT_FOUND("RSS-E201", "요청한 매퍼를 찾을 수 없습니다."),
    ITEM_MAPPING_ERROR("RSS-E202", "RSS 항목 매핑 중 오류가 발생했습니다."),

    // 데이터베이스 관련 오류
    DB_SAVE_ERROR("RSS-E301", "RSS 뉴스를 저장하는 중 오류가 발생했습니다."),
    DUPLICATE_LINK_ERROR("RSS-E302", "이미 존재하는 링크입니다."),

    // 스케줄러 관련 오류
    SCHEDULER_EXECUTION_ERROR("RSS-E401", "스케줄러 실행 중 오류가 발생했습니다.");

    private final String code;
    private final String message;
}