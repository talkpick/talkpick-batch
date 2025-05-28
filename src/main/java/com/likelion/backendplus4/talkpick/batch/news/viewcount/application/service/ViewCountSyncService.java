package com.likelion.backendplus4.talkpick.batch.news.viewcount.application.service;

import org.springframework.stereotype.Service;

import com.likelion.backendplus4.talkpick.batch.news.viewcount.application.port.in.ViewCountSyncUseCase;
import com.likelion.backendplus4.talkpick.batch.news.viewcount.application.port.out.ViewCountSyncJobPort;

import lombok.RequiredArgsConstructor;

/**
 * Redis의 조회수 데이터를 DB에 동기화합니다.
 *
 * 1. JobPort를 통해 배치 작업 실행 요청
 * 2. 비동기로 동기화 작업 수행
 *
 * @since 2025-05-25 최초 작성
 * @author 양병학
 */
@Service
@RequiredArgsConstructor
public class ViewCountSyncService implements ViewCountSyncUseCase {

    private final ViewCountSyncJobPort viewCountSyncJobPort;

    @Override
    public void syncViewCounts() { // String requestor 제거
        viewCountSyncJobPort.executeJob();
    }
}