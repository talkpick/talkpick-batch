package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.adapter;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.index.application.port.out.NewsInfoProviderPort;
import com.likelion.backendplus4.talkpick.batch.index.domain.model.NewsInfo;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.mapper.ArticleEntityMapper;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.repository.NewsInfoJpaRepository;

import lombok.RequiredArgsConstructor;

/**
 * TODO: 이벤트 기반으로 색인 안된 뉴스만 제공하도록 수정 필요
 *  NewsInfoProviderPort 인터페이스의 구현체로,
 *  JPA 리포지토리를 통해 뉴스 정보를 조회하는 어댑터 클래스입니다.
 *  현재는 최근 100개 뉴스를 반환합니다.
 * @since 2025-05-14
 */
@Component
@RequiredArgsConstructor
public class NewsInfoProviderAdapter implements NewsInfoProviderPort {
	private final NewsInfoJpaRepository newsInfoJpaRepository;

	@Override


	/**
	 * 뉴스 정보를 최신순으로 최대 100건까지 조회하여 도메인 객체 리스트로 반환합니다.
	 *
	 * @return 뉴스 도메인 객체 리스트
	 * @author 함예정
	 * @since 2025-05-14
	 */
	public List<NewsInfo> fetchAll() {
		Pageable pageable = PageRequest.of(0, 100)
			.withSort(Sort.by("pubDate").descending());

		return newsInfoJpaRepository.findAll(pageable)
			.getContent()
			.stream()
			.map(ArticleEntityMapper::toDomainFromEntity)
			.toList();
	}
}
