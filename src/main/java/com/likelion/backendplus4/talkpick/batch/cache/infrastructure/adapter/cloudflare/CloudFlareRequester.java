package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.cloudflare;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.Getter;

@Component
@Getter
public class CloudFlareRequester {
	private final RestTemplate restTemplate;
	private final String zoneId;
	private final String apiToken;

	public CloudFlareRequester(
		RestTemplate restTemplate,
		@Value("${cloudflare.zone-id}") String zoneId,
		@Value("${cloudflare.api-token}") String apiToken) {
		this.restTemplate = restTemplate;
		this.zoneId = zoneId;
		this.apiToken = apiToken;
	}


	public Map<String, Object> getBody(String key, Object value) {
		Map<String, Object> body = new HashMap<>();
		body.put(key, value);
		return body;
	}

	public ResponseEntity<Map> sendRequest(Map<String, Object> body) {
		String url = getUrl();
		HttpHeaders headers = getHeaders();
		HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
		ResponseEntity<Map> response =
			restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
		return response;
	}


	private HttpHeaders getHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(getApiToken());
		return headers;
	}

	private String getUrl() {
		return String.format("https://api.cloudflare.com/client/v4/zones/%s/purge_cache", getZoneId());
	}
}