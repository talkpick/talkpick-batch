package com.likelion.backendplus4.talkpick.batch.news.article.application.port.out;

import java.util.List;

import com.likelion.backendplus4.talkpick.batch.news.article.domain.model.NewsInfo;

/**
 * 뉴스 정보를 색인 저장소에 저장하는 포트 인터페이스
 *
 * @since 2025-05-15
 */
public interface NewsInfoIndexRepositoryPort {
	/**
	 * 뉴스 정보 리스트를 색인 저장소에 저장한다.
	 *
	 * @return 저장된 뉴스 정보 건수
	 * @author 정안식
	 * @since 2025-05-15
	 */
	void saveAll();
}
