package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.mapper;

import com.likelion.backendplus4.talkpick.batch.index.domain.model.NewsInfo;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;

public class ArticleEntityMapper {
	public static NewsInfo toDomainFromEntity(ArticleEntity articleEntity) {
		return NewsInfo.builder()
			.newsId(articleEntity.getGuid())
			.title(articleEntity.getTitle())
			.content(articleEntity.getDescription())
			.publishedAt(articleEntity.getPubDate())
			.imageUrl(articleEntity.getImageUrl())
			.category(articleEntity.getCategory())
			.summary(articleEntity.getSummary())
			.summaryVector(articleEntity.getSummaryVector())
			.build();
	}
}
