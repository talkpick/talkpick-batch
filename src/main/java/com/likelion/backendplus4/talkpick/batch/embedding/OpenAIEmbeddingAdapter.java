package com.likelion.backendplus4.talkpick.batch.embedding;

import java.util.List;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OpenAIEmbeddingAdapter implements EmbeddingPort {
	private final OpenAiApi openAiApi;
	@Value("${spring.ai.openai.embedding-model}")
	private String embeddingModel;

	@Override
	public float[] getEmbedding(String text) {
		try {
			OpenAiEmbeddingModel embeddingModel = createEmbeddingModel();
			EmbeddingResponse response = embeddingModel.embedForResponse(List.of(text));
			return response.getResults().getFirst().getOutput();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private OpenAiEmbeddingModel createEmbeddingModel() {
		return new OpenAiEmbeddingModel(
			openAiApi,
			MetadataMode.EMBED,
			OpenAiEmbeddingOptions.builder()
				.model(embeddingModel)
				.build(),
			RetryUtils.DEFAULT_RETRY_TEMPLATE
		);
	}
}
