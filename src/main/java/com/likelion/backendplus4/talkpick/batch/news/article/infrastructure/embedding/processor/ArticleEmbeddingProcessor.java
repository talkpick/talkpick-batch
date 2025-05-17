package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.embedding.processor;

import java.util.List;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.embedding.exception.EmbeddingException;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.embedding.exception.error.EmbeddingErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;

/**
 * 뉴스 기사 요약(summary)을 기반으로 임베딩 벡터를 생성하고
 * 해당 벡터를 ArticleEntity에 설정하는 ItemProcessor 구현체.
 * Spring Batch의 처리 단계에서 사용된다.
 *
 * @since 2025-05-17
 */
@Component
public class ArticleEmbeddingProcessor implements ItemProcessor<ArticleEntity, ArticleEntity> {
	private final OpenAiApi openAiApi;
	private final String embeddingModelName;

	public ArticleEmbeddingProcessor(OpenAiApi openAiApi,
		@Value("${spring.ai.openai.embedding-model}") String embeddingModelName) {
		this.openAiApi = openAiApi;
		this.embeddingModelName = embeddingModelName;
	}

	/**
	 * ArticleEntity의 summary 필드를 기반으로 임베딩 벡터를 생성하고,
	 * 해당 벡터를 summaryVector 필드에 설정하여 반환한다.
	 *
	 * @param item 임베딩할 summary를 가진 ArticleEntity
	 * @return summaryVector가 설정된 ArticleEntity
	 * @author 함예정
	 * @since 2025-05-17
	 */
	@Override
	public ArticleEntity process(ArticleEntity item) {
		String newsContent = item.getSummary();
		float[] vector = getEmbedding(newsContent);
		return item.changeSummaryVector(vector);
	}

	/**
	 * 주어진 텍스트에 대해 임베딩 벡터(float 배열)를 생성한다.
	 * 내부적으로 OpenAI 임베딩 모델을 생성하고 실행한다.
	 *
	 * @param text 임베딩할 입력 텍스트
	 * @return 텍스트에 대한 임베딩 벡터
	 * @author 정안식
	 * @since 2025-05-11
	 */
	private float[] getEmbedding(String text) {
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
