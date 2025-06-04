package com.likelion.backendplus4.talkpick.batch.common.configuration.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {


	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("TalkPick Batch API")
				.description("TalkPick 프로젝트의 Batch API 문서입니다.")
				.version("1.0.0"));
	}
}
