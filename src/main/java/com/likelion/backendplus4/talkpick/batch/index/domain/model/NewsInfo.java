package com.likelion.backendplus4.talkpick.batch.index.domain.model;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 뉴스 정보를 표현하는 도메인 모델
 *
 * @since 2025-05-15
 * @modified 2025-05-18
 */
@RequiredArgsConstructor
@Getter
@Builder
public class NewsInfo {
	private final String newsId;
	private final String title;
	private final String content;
	private final LocalDateTime publishedAt;
	private final String imageUrl;
	private final String category;
	private final String summary;
	private final float[] summaryVector;
}
