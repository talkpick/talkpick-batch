package com.likelion.backendplus4.talkpick.batch.chat.infrastructure.adapter.out.jpa.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Table(name = "chat_message")
public class ChatMessageEntity {
	@Id
	@GeneratedValue(generator = "msg_seq_gen")
	@GenericGenerator(
		name = "msg_seq_gen",
		strategy = "enhanced-sequence",
		parameters = {
			@Parameter(name = "optimizer",       value = "pooled-lo"),
			@Parameter(name = "initial_value",   value = "1"),
			@Parameter(name = "increment_size",  value = "50")
		}
	)
	@Column(name = "chat_message_id")
	private Long id;
	@Column(name = "article_id")
	private String articleId;
	@Column(name = "sender")
	private String sender;
	@Column(name = "content")
	private String content;
	@Column(name = "timestamp")
	private LocalDateTime timestamp;
}
