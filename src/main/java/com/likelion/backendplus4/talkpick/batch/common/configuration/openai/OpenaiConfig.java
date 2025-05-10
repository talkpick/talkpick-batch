package com.likelion.backendplus4.talkpick.batch.common.configuration.openai;

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
	@Value("${spring.ai.openai.api-key}")
	private String apiKey;

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
}
