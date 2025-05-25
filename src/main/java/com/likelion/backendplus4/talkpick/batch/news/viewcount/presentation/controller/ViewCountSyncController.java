package com.likelion.backendplus4.talkpick.batch.news.article.presentation.controller;

import static com.likelion.backendplus4.talkpick.batch.common.response.ApiResponse.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.backendplus4.talkpick.batch.common.response.ApiResponse;
import com.likelion.backendplus4.talkpick.batch.news.viewcount.application.port.in.ViewCountSyncUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 조회수 동기화 관리 컨트롤러.
 * Redis의 조회수 데이터를 DB에 동기화하는 배치 작업을 수동으로 실행할 수 있는 API를 제공합니다.
 *
 * @since 2025-05-25
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/news/viewcount")
public class ViewCountSyncController {

    private final ViewCountSyncUseCase viewCountSyncUseCase;

    /**
     * Redis의 조회수 데이터를 수동으로 DB에 동기화합니다.
     * 관리자가 필요에 따라 수동으로 동기화 작업을 실행할 수 있습니다.
     *
     * @return 성공 응답
     * @since 2025-05-25
     */
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<String>> syncViewCount() {

        log.info("조회수 동기화 수동 실행 요청");

        viewCountSyncUseCase.syncViewCounts();

        return success("조회수 동기화가 시작되었습니다.");
    }
}