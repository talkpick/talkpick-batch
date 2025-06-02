package com.likelion.backendplus4.talkpick.batch.cache.application.service;

import org.springframework.stereotype.Service;

import com.likelion.backendplus4.talkpick.batch.cache.application.port.in.CacheUseCase;
import com.likelion.backendplus4.talkpick.batch.cache.application.port.out.CacheResetPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CacheService implements CacheUseCase {
	private static final String LATEST_NEWS_PREFIX = "https://talkpick.techlog.dev/api/public/news/latest";
	private final CacheResetPort cacheResetPort;

	@Override
	public boolean clearCacheByLatestNews(){
		return cacheResetPort.purgePrefix(LATEST_NEWS_PREFIX);
	}
}
