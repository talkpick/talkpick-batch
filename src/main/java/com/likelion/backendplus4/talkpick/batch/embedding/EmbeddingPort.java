package com.likelion.backendplus4.talkpick.batch.embedding;

public interface EmbeddingPort {

	float[] getEmbedding(String text);
}
