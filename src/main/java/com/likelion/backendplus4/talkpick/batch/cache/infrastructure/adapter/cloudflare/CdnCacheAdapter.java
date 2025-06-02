package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.cloudflare;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CdnCacheAdapter {
	private final CloudFlareRequester cloudFlareRequester;

	public Map<String, Object> purgePrefix(String prefix) {
		Map<String, Object> body = cloudFlareRequester.getBody("prefixes", prefix);
		ResponseEntity<Map> response = cloudFlareRequester.sendRequest(body);
		return response.getBody();
	}

	public Map<String, Object> purgeFile(List<String> fileUrls) {
		Map<String, Object> body = cloudFlareRequester.getBody("files", fileUrls);
		ResponseEntity<Map> response = cloudFlareRequester.sendRequest(body);
		return response.getBody();
	}

	public Map<String, Object> purgeEverything() {
		Map<String, Object> body = cloudFlareRequester.getBody("purge_everything", true);
		ResponseEntity<Map> response = cloudFlareRequester.sendRequest(body);
		return response.getBody();
	}
}
