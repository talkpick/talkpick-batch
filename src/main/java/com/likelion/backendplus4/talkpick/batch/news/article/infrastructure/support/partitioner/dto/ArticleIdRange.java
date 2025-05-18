package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.support.partitioner.dto;

/**
 * ID 범위(start, end)를 표현하는 불변 타입 DTO
 *
 * @since 2025-05-18
 */
public record ArticleIdRange(long start, long end) {}
