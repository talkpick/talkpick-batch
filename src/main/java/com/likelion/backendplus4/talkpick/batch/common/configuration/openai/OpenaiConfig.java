package com.likelion.backendplus4.talkpick.batch.common.configuration.openai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAI API 클라이언트 빈을 생성하는 구성 클래스
 *
 * @since 2025-05-11
 */
@Configuration
public class OpenaiConfig {
	private final String apiKey;
	private final String chatModelName;
	private final Double temperature;
	private final Integer maxToken;

	public OpenaiConfig(
		@Value("${spring.ai.openai.api-key}") String apiKey,
		@Value("${spring.ai.openai.summary.model}") String chatModelName,
		@Value("${spring.ai.openai.summary.temperature}") Double temperature,
		@Value("${spring.ai.openai.summary.maxCompletionTokens}") Integer maxToken) {
		this.apiKey = apiKey;
		this.chatModelName = chatModelName;
		this.temperature = temperature;
		this.maxToken = maxToken;
	}

	/**
	 * OpenAiApi 빈을 생성한다.
	 *
	 * @return OpenAI API 클라이언트 인스턴스
	 * @author 정안식
	 * @since 2025-05-11
	 */
	@Bean
	public OpenAiApi openaiApi() {
		return new OpenAiApi(apiKey);
	}

	@Bean
	public ChatClient chatClient(OpenAiChatModel chatModel) {
		return ChatClient.create(chatModel);
	}

	@Bean
	public OpenAiChatModel chatModel(OpenAiApi openAiApi) {
		OpenAiChatOptions options = OpenAiChatOptions.builder()
			.model(chatModelName)
			.temperature(temperature)
			.maxCompletionTokens(maxToken)
			.build();
		return new OpenAiChatModel(openAiApi, options);
	}
}
