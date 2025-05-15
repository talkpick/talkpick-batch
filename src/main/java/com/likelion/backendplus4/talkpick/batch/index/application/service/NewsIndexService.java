package com.likelion.backendplus4.talkpick.batch.index.application.service;

import java.util.List;

import org.elasticsearch.index.IndexService;
import org.springframework.stereotype.Service;

import com.likelion.backendplus4.talkpick.batch.common.annotation.logging.EntryExitLog;
import com.likelion.backendplus4.talkpick.batch.common.annotation.logging.TimeTracker;
import com.likelion.backendplus4.talkpick.batch.index.application.port.in.NewsIndexUseCase;
import com.likelion.backendplus4.talkpick.batch.index.application.port.out.NewsInfoIndexRepositoryPort;
import com.likelion.backendplus4.talkpick.batch.index.application.port.out.NewsInfoProviderPort;
import com.likelion.backendplus4.talkpick.batch.index.domain.model.NewsInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 뉴스 정보를 조회하고 색인 저장소에 전달하는 비즈니스 로직 서비스
 *
 * @since 2025-05-15
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class NewsIndexService implements NewsIndexUseCase {
	private final NewsInfoProviderPort newsInfoProviderPort;
	private final NewsInfoIndexRepositoryPort newsInfoIndexRepositoryPort;

	/**
	 * 모든 뉴스 정보를 가져와 색인 저장소에 저장하고 저장된 건수를 반환한다.
	 *
	 * @return 색인된 뉴스 정보 건수
	 * @author 정안식
	 * @since 2025-05-15
	 */
	@EntryExitLog()
	@Override
	public int indexAllNewsInfo() {
		List<NewsInfo> newsInfoList = newsInfoProviderPort.fetchAll();
		return newsInfoIndexRepositoryPort.saveAll(newsInfoList);
	}
}
