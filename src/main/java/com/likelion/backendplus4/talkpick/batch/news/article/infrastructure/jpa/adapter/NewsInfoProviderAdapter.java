package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.adapter;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.index.application.port.out.NewsInfoProviderPort;
import com.likelion.backendplus4.talkpick.batch.index.domain.model.NewsInfo;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.repository.NewsInfoJpaRepository;

import lombok.RequiredArgsConstructor;

/**
 * TODO: 이벤트 기반으로 색인 안된 뉴스만 제공하도록 수정 필요
 * 최근 100개 뉴스 정보를 반환합니다.
 *
 * @since 2025-05-14
 */
@Component
@RequiredArgsConstructor
public class NewsInfoProviderAdapter implements NewsInfoProviderPort {
	private final NewsInfoJpaRepository newsInfoJpaRepository;

	@Override

	public List<NewsInfo> fetchAll() {
		Pageable pageable = PageRequest.of(0, 100)
			.withSort(Sort.by("pubDate").descending());
		newsInfoJpaRepository.findAll(pageable).getContent()
			.stream()
			.map(newsInfo -> newsInfo);
		return
	}
}
