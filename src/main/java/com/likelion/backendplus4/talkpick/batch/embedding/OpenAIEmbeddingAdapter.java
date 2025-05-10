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

/**
 * OpenAI API를 사용하여 텍스트 임베딩을 생성하는 어댑터 구현체
 *
 * @since 2025-05-11
 */
@Component
public class OpenAIEmbeddingAdapter implements EmbeddingPort {
	private final OpenAiApi openAiApi;
	private final String embeddingModelName;

	public OpenAIEmbeddingAdapter(OpenAiApi openAiApi,
		@Value("${spring.ai.openai.embedding-model}") String embeddingModelName) {
		this.openAiApi = openAiApi;
		this.embeddingModelName = embeddingModelName;
	}

	/**
	 * 주어진 텍스트에 대한 임베딩 벡터를 반환한다.
	 *
	 * @param text 입력 텍스트
	 * @return 텍스트 임베딩 벡터 배열
	 * @since 2025-05-11
	 */
	@EntryExitLog
	@LogMethodValues
	@TimeTracker
	@Override
	public float[] getEmbedding(String text) {
		OpenAiEmbeddingModel model = createModel();
		return executeEmbedding(model, text);
	}

	/**
	 * OpenAI 임베딩 모델 인스턴스를 생성한다.
	 *
	 * @return 생성된 OpenAiEmbeddingModel 객체
	 * @throws EmbeddingException 모델 생성 중 오류 발생 시
	 * @author 정안식
	 * @since 2025-05-11
	 */
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

	/**
	 * 주어진 모델을 사용하여 텍스트 임베딩을 계산한다.
	 *
	 * @param model OpenAiEmbeddingModel 인스턴스
	 * @param text  입력 텍스트
	 * @return 계산된 임베딩 벡터 배열
	 * @throws EmbeddingException API 호출 중 오류 발생 시
	 * @author 정안식
	 * @since 2025-05-11
	 */
	private float[] executeEmbedding(OpenAiEmbeddingModel model, String text) {
		try {
			EmbeddingResponse response = model.embedForResponse(List.of(text));
			return response.getResults().getFirst().getOutput();
		} catch (Exception e) {
			throw new EmbeddingException(EmbeddingErrorCode.API_CALL_ERROR, e);
		}
	}
}
