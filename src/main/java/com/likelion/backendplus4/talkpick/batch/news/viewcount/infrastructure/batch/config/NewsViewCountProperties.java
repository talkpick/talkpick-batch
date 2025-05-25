package com.likelion.backendplus4.talkpick.batch.news.viewcount.infrastructure.batch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * 유효한 뉴스 ID 접두사 목록 (쉼표로 구분): application.yml에서 관리
 * @since 2025-05-20
 * @author 양병학
 */
@Configuration
@ConfigurationProperties(prefix = "news.viewcount")
@Getter
@Setter
public class NewsViewCountProperties {
    private String validPrefixes;
}