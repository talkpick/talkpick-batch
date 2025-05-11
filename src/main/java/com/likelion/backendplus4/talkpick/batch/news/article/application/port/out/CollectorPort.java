package com.likelion.backendplus4.talkpick.batch.news.article.application.port.out;

public interface CollectorPort {
	boolean start();
	boolean stop();
	boolean isRunning();
}
