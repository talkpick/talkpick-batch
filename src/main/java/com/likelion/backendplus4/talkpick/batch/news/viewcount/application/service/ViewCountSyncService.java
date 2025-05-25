package com.likelion.backendplus4.talkpick.batch.news.viewcount.application.service;

import org.springframework.stereotype.Service;

import com.likelion.backendplus4.talkpick.batch.news.viewcount.application.port.in.ViewCountSyncUseCase;
import com.likelion.backendplus4.talkpick.batch.news.viewcount.application.port.out.ViewCountSyncJobPort;

import lombok.RequiredArgsConstructor;

/**
 * 조회수 동기화 작업을 처리하는 서비스 클래스입니다.
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