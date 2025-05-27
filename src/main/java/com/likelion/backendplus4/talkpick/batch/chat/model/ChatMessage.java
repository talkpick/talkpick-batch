package com.likelion.backendplus4.talkpick.batch.chat.model;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessage {
    private final Long chatId;
    private final String articleId;
    private final String sender;
    private final String content;
    private final LocalDateTime timestamp;
    private final MessageType messageType;

}