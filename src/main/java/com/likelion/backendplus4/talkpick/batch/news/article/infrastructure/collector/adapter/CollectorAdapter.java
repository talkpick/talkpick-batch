package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.adapter;

import java.util.stream.IntStream;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.application.port.out.CollectorPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CollectorAdapter implements CollectorPort {
	private final Scheduler scheduler;

	@Override
	public boolean start() {
		try {
			scheduler.start();
			return isRunning();
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public boolean stop() {
		try {
			if (!scheduler.isInStandbyMode()) {
				scheduler.standby();
			}
			return true;
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isRunning() {
		return IntStream.range(0, 3)
			.peek(i -> {
					sleep(300L);
				})
			.anyMatch(i -> isRunning());
	}

	private void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
