package com.likelion.backendplus4.talkpick.batch.index.domain.model;

import java.time.LocalDateTime;

public record NewsInfo(
	String newsId,
	String title,
	String content,
	LocalDateTime publishedAt,
	String imageUrl
) {}
