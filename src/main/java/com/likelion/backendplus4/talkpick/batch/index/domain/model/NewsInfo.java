package com.likelion.backendplus4.talkpick.batch.index.domain.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class NewsInfo{
	private final String newsId;
	private final String title;
	private final String content;
	private final LocalDateTime publishedAt;
	private final String imageUrl;
	}
