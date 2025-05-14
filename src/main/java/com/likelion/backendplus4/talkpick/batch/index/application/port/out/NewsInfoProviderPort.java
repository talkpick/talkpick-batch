package com.likelion.backendplus4.talkpick.batch.index.application.port.out;

import java.util.List;

import com.likelion.backendplus4.talkpick.batch.index.domain.model.NewsInfo;

public interface NewsInfoProviderPort {
	List<NewsInfo> fetchAll();
}
