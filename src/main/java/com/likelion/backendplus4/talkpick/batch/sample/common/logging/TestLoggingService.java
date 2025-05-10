package com.likelion.backendplus4.talkpick.batch.sample.common.logging;

import org.springframework.stereotype.Service;

import com.likelion.backendplus4.talkpick.batch.common.annotation.logging.EntryExitLog;
import com.likelion.backendplus4.talkpick.batch.common.annotation.logging.LogMethodValues;
import com.likelion.backendplus4.talkpick.batch.common.annotation.logging.TimeTracker;

@Service
public class TestLoggingService {
	@EntryExitLog
	@LogMethodValues
	@TimeTracker
	public String test(String text) {
		System.out.println("TestService 요청 성공");
		return "bye";
	}
}
