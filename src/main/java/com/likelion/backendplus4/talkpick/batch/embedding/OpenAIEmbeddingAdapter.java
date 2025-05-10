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

import com.likelion.backendplus4.talkpick.batch.common.annotation.logging.EntryExitLog;
import com.likelion.backendplus4.talkpick.batch.common.annotation.logging.LogMethodValues;
import com.likelion.backendplus4.talkpick.batch.common.annotation.logging.TimeTracker;
import com.likelion.backendplus4.talkpick.batch.embedding.exception.EmbeddingException;
import com.likelion.backendplus4.talkpick.batch.embedding.exception.error.EmbeddingErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OpenAIEmbeddingAdapter implements EmbeddingPort {
	private final OpenAiApi openAiApi;
	@Value("${spring.ai.openai.embedding-model}")
	private String embeddingModelName;

	@EntryExitLog
	@LogMethodValues
	@TimeTracker
	@Override
	public float[] getEmbedding(String text) {
		OpenAiEmbeddingModel model = createModel();
		return executeEmbedding(model, text);
	}

	private OpenAiEmbeddingModel createModel() {
		try {
			return new OpenAiEmbeddingModel(
				openAiApi,
				MetadataMode.EMBED,
				OpenAiEmbeddingOptions.builder()
					.model(embeddingModelName)
					.build(),
				RetryUtils.DEFAULT_RETRY_TEMPLATE
			);
		} catch (Exception e) {
			throw new EmbeddingException(EmbeddingErrorCode.MODEL_CREATION_ERROR, e);
		}
	}

	private float[] executeEmbedding(OpenAiEmbeddingModel model, String text) {
		try {
			EmbeddingResponse response = model.embedForResponse(List.of(text));
			return response.getResults().getFirst().getOutput();
		} catch (Exception e) {
			throw new EmbeddingException(EmbeddingErrorCode.API_CALL_ERROR, e);
		}
	}
}
