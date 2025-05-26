package com.likelion.backendplus4.talkpick.batch.chat.support.mapper;

import com.likelion.backendplus4.talkpick.batch.chat.infrastructure.adapter.out.jpa.entity.ChatMessageEntity;
import com.likelion.backendplus4.talkpick.batch.chat.model.ChatMessage;

/**
 * Chat 관련 매퍼 클래스입니다.
 *
 * @since 2025-05-26
 */
public class ChatMessageMapper {

	/**
	 * ChatMessage 도메인 객체를 ChatMessageEntity 엔티티 객체로 변환합니다.
	 *
	 * @param chatMessage 변환할 ChatMessage 도메인 객체
	 * @return 변환된 ChatMessageEntity 엔티티 객체
	 *
	 * @author 박찬병
	 * @since 2025-05-26
	 */
	public static ChatMessageEntity toEntityFromDomain(ChatMessage chatMessage) {
		return ChatMessageEntity.builder()
			.articleId(chatMessage.getArticleId())
			.sender(chatMessage.getSender())
			.content(chatMessage.getContent())
			.timestamp(chatMessage.getTimestamp())
			.build();
	}

}
