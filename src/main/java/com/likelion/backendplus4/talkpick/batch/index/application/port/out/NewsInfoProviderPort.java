package com.likelion.backendplus4.talkpick.batch.index.application.port.out;

import java.util.List;

import com.likelion.backendplus4.talkpick.batch.index.domain.model.NewsInfo;

/**
 * 외부 또는 내부에서 뉴스 정보를 조회하는 포트 인터페이스
 *
 * @since 2025-05-15
 */
public interface NewsInfoProviderPort {
	/**
	 * 저장된 모든 뉴스 정보를 조회한다.
	 *
	 * @return 조회된 뉴스 정보 리스트
	 * @author 정안식
	 * @since 2025-05-15
	 */
	List<NewsInfo> fetchAll();
}
