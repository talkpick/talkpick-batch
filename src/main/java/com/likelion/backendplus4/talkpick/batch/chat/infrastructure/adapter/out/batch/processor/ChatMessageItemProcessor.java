package com.likelion.backendplus4.talkpick.batch.chat.infrastructure.adapter.out.batch.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.backendplus4.talkpick.batch.chat.exception.ChatBatchException;
import com.likelion.backendplus4.talkpick.batch.chat.exception.error.ChatBatchErrorCode;
import com.likelion.backendplus4.talkpick.batch.chat.infrastructure.adapter.out.jpa.entity.ChatMessageEntity;
import com.likelion.backendplus4.talkpick.batch.chat.model.ChatMessage;
import com.likelion.backendplus4.talkpick.batch.chat.support.mapper.ChatMessageMapper;

import lombok.RequiredArgsConstructor;

/**
 * Redis 스트림에서 읽은 레코드를 도메인 ChatMessage로 역직렬화한 후,
 * 이를 ChatMessageEntity로 매핑하는 Processor 클래스입니다.
 *
 * @since 2025-05-27
 */
@Component
@RequiredArgsConstructor
public class ChatMessageItemProcessor
	implements ItemProcessor<MapRecord<String,String,String>, ChatMessageEntity> {

	private final ObjectMapper objectMapper;
	private static final String PAYLOAD = "payload";

	/**
	 * Redis 스트림 레코드를 ChatMessage 도메인 객체로 변환하고,
	 * 이를 JPA 엔티티로 매핑합니다.
	 *
	 * @param record Redis 스트림에서 읽은 MapRecord
	 * @return 매핑된 ChatMessageEntity 객체
	 * @throws ChatBatchException 변환 또는 매핑 중 오류 발생 시
	 * @author 박찬병
	 * @since 2025-05-27
	 */
	@Override
	public ChatMessageEntity process(MapRecord<String, String, String> record) {
		try {
			ChatMessage domain = objectMapper.readValue(
				record.getValue().get(PAYLOAD), ChatMessage.class);
			return ChatMessageMapper.toEntityFromDomain(domain);
		} catch (Exception e) {
			throw new ChatBatchException(
				ChatBatchErrorCode.STREAM_READ_ERROR, e);
		}
	}
}