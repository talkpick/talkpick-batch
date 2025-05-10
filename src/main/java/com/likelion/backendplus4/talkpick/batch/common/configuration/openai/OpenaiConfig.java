package com.likelion.backendplus4.talkpick.batch.common.configuration.openai;

import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenaiConfig {
	@Value("${spring.ai.openai.api-key}")
	private String apiKey;

	@Bean
	public OpenAiApi openaiApi() {
		return new OpenAiApi(apiKey);
	}
}
