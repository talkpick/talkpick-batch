package com.likelion.backendplus4.talkpick.batch.cache.infrastructure.adapter.cloudflare;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.cache.application.port.out.CacheResetPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CdnCacheAdapter implements CacheResetPort {
	private final CloudFlareRequester cloudFlareRequester;

	@Override
	public boolean purgePrefix(List<String> prefix) {
		Map<String, Object> body = cloudFlareRequester.getBody("prefixes", prefix);
		return responseBySendRequest(body);
	}



	@Override
	public boolean purgeFile(List<String> fileUrls) {
		Map<String, Object> body = cloudFlareRequester.getBody("files", fileUrls);
		return responseBySendRequest(body);
	}

	@Override
	public boolean purgeEverything() {
		Map<String, Object> body = cloudFlareRequester.getBody("purge_everything", true);
		return responseBySendRequest(body);
	}
	private boolean responseBySendRequest(Map<String, Object> body) {
		return cloudFlareRequester.sendResetRequest(body) == 200 ? true : false;
	}
}
