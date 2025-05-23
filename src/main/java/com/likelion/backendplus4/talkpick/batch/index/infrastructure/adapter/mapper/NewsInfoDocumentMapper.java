package com.likelion.backendplus4.talkpick.batch.index.infrastructure.adapter.mapper;

import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.index.domain.model.NewsInfo;
import com.likelion.backendplus4.talkpick.batch.index.infrastructure.adapter.document.NewsInfoDocument;

/**
 * 도메인 모델 NewsInfo를 Elasticsearch 문서 모델로 변환하는 매퍼
 *
 * @since 2025-05-15
 * @modified 2025-05-19
 */
@Component
public class NewsInfoDocumentMapper {
	/**
	 * NewsInfo 도메인 객체를 NewsInfoDocument로 변환한다.
	 *
	 * @param news 변환할 도메인 객체
	 * @return 변환된 문서 객체
	 * @author 정안식
	 * @since 2025-05-15
	 * @modified 2025-05-19
	 * 25-05-19 - summary, summaryVector 필드 추가
	 */
	public NewsInfoDocument toDocument(NewsInfo news) {
		return new NewsInfoDocument(
			news.getNewsId(),
			news.getTitle(),
			news.getContent(),
			news.getPublishedAt(),
			news.getImageUrl(),
			news.getCategory(),
			news.getSummary(),
			news.getSummaryVector()
		);
	}
}

