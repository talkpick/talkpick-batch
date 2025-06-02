package com.likelion.backendplus4.talkpick.batch.news.viewcount.application.port.in;

/**
 * Redis 정리 작업을 위한 Use Case 인터페이스입니다.
 *
 * @since 2025-05-29
 */
public interface ViewRankingCleanupUseCase {

    /**
     * 3일 이상 지난 뉴스 랭킹 키들을 정리합니다.
     *
     * @author 양병학
     * @since 2025-05-29
     */
    void cleanupOldRankingKeys();
}
