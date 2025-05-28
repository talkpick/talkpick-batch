package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.quartz.adapter.job;

import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class JobTestCtrl {
	private final IndexScheduleJob indexScheduleJob;
	@GetMapping("/test")
	public String test() {
		indexScheduleJob.execute(null);
		return "test";
	}
}
