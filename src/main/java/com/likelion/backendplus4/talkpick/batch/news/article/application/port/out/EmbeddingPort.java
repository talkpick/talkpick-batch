package com.likelion.backendplus4.talkpick.batch.news.article.application.port.out;

public interface EmbeddingPort {

	/**
	 * 주어진 텍스트에 대한 임베딩 벡터를 반환한다.
	 *
	 * @param text 입력 텍스트
	 * @return 텍스트 임베딩 벡터 배열
	 * @since 2025-05-11
	 */
	float[] getEmbedding(String text);
}
