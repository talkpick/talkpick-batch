package com.likelion.backendplus4.talkpick.batch.sample.index;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.common.annotation.logging.EntryExitLog;
import com.likelion.backendplus4.talkpick.batch.common.annotation.logging.LogMethodValues;
import com.likelion.backendplus4.talkpick.batch.common.annotation.logging.TimeTracker;
import com.likelion.backendplus4.talkpick.batch.index.application.port.out.NewsInfoProviderPort;
import com.likelion.backendplus4.talkpick.batch.index.domain.model.NewsInfo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NewsIndexServiceTestImpl implements NewsInfoProviderPort{

	@EntryExitLog
	@TimeTracker
	@LogMethodValues
	@Override
	public List<NewsInfo> fetchAll() {
		return List.of(
			new NewsInfo(
				"news-1",
				"테스트 뉴스 1",
				"첫 번째 테스트 뉴스의 내용입니다.",
				LocalDateTime.of(2025, 5, 14, 10, 0),
				"https://example.com/image1.jpg",
				"테스트"
			),
			new NewsInfo(
				"news-2",
				"테스트 뉴스 2",
				"두 번째 테스트 뉴스의 내용입니다.",
				LocalDateTime.of(2025, 5, 13, 11, 30),
				"https://example.com/image2.jpg",
				"테스트"
			),
			new NewsInfo(
				"news-3",
				"테스트 뉴스 3",
				"세 번째 테스트 뉴스의 내용입니다.",
				LocalDateTime.of(2025, 5, 12, 14, 45),
				"https://example.com/image3.jpg",
				"테스트"
			)
		);
	}
}

