package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.writer;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.repository.NewsInfoJpaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 기사 데이터를 DB에 저장하는 Spring Batch ItemWriter 구현체.
 * 중복된 링크는 저장하지 않으며, 새롭게 저장된 기사 수를 로그로 출력한다.
 * 매퍼에서 전달된 문단 구분자(PARAGRAPH_BREAK)를 기준으로 문단을 분리하고 직렬화한다.
 *
 * - 입력: 기사 리스트(List<ArticleEntity>)
 * - 처리: 문단 분리, 직렬화, 중복 여부 확인 후 저장
 * - 출력: 로그 출력 (중복 제외)
 *
 * @since 2025-05-10
 * @modified 2025-05-15 직렬화 확인 로직 추가
 * @modified 2025-05-16 PARAGRAPH_BREAK 기반 문단 처리 추가
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleWriter implements ItemWriter<List<ArticleEntity>> {

	private final NewsInfoJpaRepository newsInfoJpaRepository;
	private static final String PARAGRAPH_BREAK = "PARAGRAPH_BREAK";

	/**
	 * 기사 리스트를 저장하며, 중복된 기사는 건너뛴다.
	 * 저장 성공 시 개수를 집계하고, 로그로 남긴다.
	 *
	 * @param chunk Spring Batch가 전달하는 기사 리스트 Chunk
	 * @since 2025-05-10
	 * @author 함예정
	 */
	@Override
	public void write(Chunk<? extends List<ArticleEntity>> chunk) {
		AtomicInteger savedCount = new AtomicInteger();
		chunk.getItems().stream()
			.flatMap(List::stream)
			.peek(this::processAndSerializeDescription)
			.filter(item -> !newsInfoJpaRepository.existsByLink(item.getLink()))
			.forEach(item -> {saveItem(item, savedCount);});

		log.info("새로 저장된 뉴스 개수: {}", savedCount.get());
	}

	/**
	 * 설명(description) 필드를 처리하고 JSON 형식으로 직렬화
	 * 매퍼에서 전달된 PARAGRAPH_BREAK를 기준으로 문단을 분리하기
	 *
	 * @param item 처리할 ArticleEntity 객체
	 */
	private void processAndSerializeDescription(ArticleEntity item) {
		String description = item.getDescription();
		if (description == null || description.isEmpty()) {
			item.setDescription("[]");
			return;
		}

		if (isAlreadyJsonFormat(description)) {
			return;
		}

		List<String> paragraphs = splitIntoParagraphs(description);
		String jsonDescription = serializeToJson(paragraphs);
		item.setDescription(jsonDescription);

		log.info("직렬화 완료: {}",
				jsonDescription.substring(0, Math.min(jsonDescription.length(), 50)) + "...");
	}

	/**
	 * 문자열이 JSON 형식인지 확인
	 *
	 * @param text 확인할 문자열
	 * @return JSON 형식이면 true, 아니면 false
	 */
	private boolean isAlreadyJsonFormat(String text) {
		return text.trim().startsWith("[") && text.trim().endsWith("]");
	}

	/**
	 * 텍스트를 PARAGRAPH_BREAK를 기준으로 문단으로 분리
	 *
	 * @param text 분리할 텍스트
	 * @return 분리된 문단 리스트
	 */
	private List<String> splitIntoParagraphs(String text) {
		if (!text.contains(PARAGRAPH_BREAK)) {
			log.warn("PARAGRAPH_BREAK 구분자가 없는 텍스트 감지: {}",
					text.substring(0, Math.min(text.length(), 50)) + "...");
			return Arrays.asList(text);
		}

		String[] paragraphArray = text.split(PARAGRAPH_BREAK);
		List<String> paragraphs = new ArrayList<>();

		for (String paragraph : paragraphArray) {
			String trimmed = paragraph.trim();
			if (!trimmed.isEmpty()) {
				paragraphs.add(trimmed);
			}
		}

		if (paragraphs.isEmpty()) {
			paragraphs.add(text);
		}

		return paragraphs;
	}

	/**
	 * 문단 리스트를 JSON으로 직렬화한다.
	 *
	 * @param paragraphs 직렬화할 문단 리스트
	 * @return JSON 형식의 문자열
	 */
	private String serializeToJson(List<String> paragraphs) {
		if (paragraphs == null || paragraphs.isEmpty()) {
			return "[]";
		}

		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < paragraphs.size(); i++) {
			String paragraph = paragraphs.get(i);
			String escaped = escapeJsonString(paragraph);
			sb.append("\"").append(escaped).append("\"");
			if (i < paragraphs.size() - 1) {
				sb.append(",");
			}
		}
		sb.append("]");

		return sb.toString();
	}

	/**
	 * JSON 문자열 이스케이프 처리
	 *
	 * @param input 이스케이프할 문자열
	 * @return 이스케이프된 문자열
	 */
	private String escapeJsonString(String input) {
		if (input == null) return "";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			switch (c) {
				case '\"': sb.append("\\\""); break;
				case '\\': sb.append("\\\\"); break;
				case '/': sb.append("\\/"); break;
				case '\b': sb.append("\\b"); break;
				case '\f': sb.append("\\f"); break;
				case '\n': sb.append("\\n"); break;
				case '\r': sb.append("\\r"); break;
				case '\t': sb.append("\\t"); break;
				default:
					if (c < ' ') {
						String hex = Integer.toHexString(c);
						sb.append("\\u");
						for (int j = 0; j < 4 - hex.length(); j++) {
							sb.append('0');
						}
						sb.append(hex);
					} else {
						sb.append(c);
					}
			}
		}
		return sb.toString();
	}

	/**
	 * DB에 뉴스를 저장하고, 저장된 개수를 증가시킨다.
	 *
	 * @param item 저장할 뉴스
	 * @param savedCount 저장된 갯수
	 * @author 함예정
	 * @since 2025-05-12
	 */
	private void saveItem(ArticleEntity item, AtomicInteger savedCount) {
		try {
			newsInfoJpaRepository.save(item);
			savedCount.incrementAndGet();
		} catch (DataIntegrityViolationException e) {
			log.debug("중복 항목 감지: {}", item.getLink());
		}
	}
}