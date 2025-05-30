package com.likelion.backendplus4.talkpick.batch.news.article.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.likelion.backendplus4.talkpick.batch.common.annotation.logging.EntryExitLog;
import com.likelion.backendplus4.talkpick.batch.news.article.application.port.in.NewsIndexUseCase;
import com.likelion.backendplus4.talkpick.batch.news.article.application.port.out.NewsInfoIndexRepositoryPort;
import com.likelion.backendplus4.talkpick.batch.news.article.application.port.out.NewsInfoProviderPort;
import com.likelion.backendplus4.talkpick.batch.news.article.domain.model.NewsInfo;

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
	private final NewsInfoIndexRepositoryPort newsInfoIndexRepositoryPort;

	/**
	 * 모든 뉴스 정보를 가져와 색인 저장소에 저장하고 저장된 건수를 반환한다.
	 *
	 * @return 색인된 뉴스 정보 건수
	 * @author 정안식
	 * @since 2025-05-15
	 * @modified 2025-05-29 함예정
	 * 25-05-29 - 쿼츠 전환으로 저장된 개수 반환 불가에 따른 반환 타입 수정 (int -> void)
	 */
	@EntryExitLog()
	@Override
	public void indexAllNewsInfo() {
		newsInfoIndexRepositoryPort.saveAll();
	}
}
