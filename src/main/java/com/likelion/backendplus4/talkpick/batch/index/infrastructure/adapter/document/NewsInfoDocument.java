package com.likelion.backendplus4.talkpick.batch.index.infrastructure.adapter.document;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Elasticsearch에 저장될 뉴스 정보 문서 모델 클래스
 *
 * @since 2025-05-15
 */
@Getter
@AllArgsConstructor
public class NewsInfoDocument {
	private final String newsId;
	private final String title;
	private final String content;
	private final LocalDateTime publishedAt;
	private final String imageUrl;
	private final String category;

	public static final String FIELD_ID           = "newsId";
	public static final String FIELD_TITLE        = "title";
	public static final String FIELD_CONTENT      = "content";
	public static final String FIELD_PUBLISHED_AT = "publishedAt";
	public static final String FIELD_IMAGE_URL    = "imageUrl";
	public static final String FIELD_CATEGORY     = "category";

	public static final String ANALYZER_NORI      = "nori";
	public static final String FIELD_KEYWORD      = "keyword";
}

