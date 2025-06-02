package com.likelion.backendplus4.talkpick.batch.news.viewcount.presentation.controller;

import com.likelion.backendplus4.talkpick.batch.news.viewcount.application.port.in.ViewRankingCleanupUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Redis 정리 작업을 수동으로 실행할 수 있는 관리자용 컨트롤러입니다.
 *
 * @since 2025-05-29 최초 작성
 * @modified 2025-06-01 현재 news.info 패키지 구조에 맞게 이동
 *                      ViewRankingCleanupUseCase와 연결
 */
@RestController
@RequestMapping("/api/admin/news")
@RequiredArgsConstructor
public class viewRankingCleanController {

    private final ViewRankingCleanupUseCase viewRankingCleanupUseCase;

    /**
     * Redis 랭킹 정리 작업을 수동으로 실행합니다.
     *
     * @return 실행 결과 메시지
     * @author 양병학
     * @since 2025-05-29
     */
    @PostMapping("/ranking/cleanup")
    public ResponseEntity<String> executeManualCleanup() {
        viewRankingCleanupUseCase.cleanupOldRankingKeys();
        return ResponseEntity.ok("Redis viewRanking 3일 이상 데이터 처리가 완료되었습니다.");
    }
}