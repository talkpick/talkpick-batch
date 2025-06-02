package com.likelion.backendplus4.talkpick.batch.cache.application.port.out;

import java.util.List;

public interface CacheResetPort {
	boolean purgePrefix(String prefix);

	boolean purgeFile(List<String> fileUrls);

	boolean purgeEverything();
}
