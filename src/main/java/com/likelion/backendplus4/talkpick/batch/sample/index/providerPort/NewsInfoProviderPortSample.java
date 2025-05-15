package com.likelion.backendplus4.talkpick.batch.sample.index.providerPort;

import static com.likelion.backendplus4.talkpick.batch.common.response.ApiResponse.*;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.backendplus4.talkpick.batch.common.response.ApiResponse;
import com.likelion.backendplus4.talkpick.batch.index.application.port.out.NewsInfoProviderPort;
import com.likelion.backendplus4.talkpick.batch.index.domain.model.NewsInfo;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sample/news/info")
public class NewsInfoProviderPortSample {
	private final NewsInfoProviderPort newsInfoProviderPort;

	/**
	 * 실제 사용시에는 Response 객체로 변환 필요
	 */
	@GetMapping
	public ResponseEntity<ApiResponse<List<NewsInfo>>> fetchAll() {
		return success(newsInfoProviderPort.fetchAll());
	}
}
