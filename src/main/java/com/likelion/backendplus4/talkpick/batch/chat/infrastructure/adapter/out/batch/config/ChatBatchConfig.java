package com.likelion.backendplus4.talkpick.batch.chat.infrastructure.adapter.out.batch.config;

import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.transaction.PlatformTransactionManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.backendplus4.talkpick.batch.chat.infrastructure.adapter.out.batch.reader.RedisStreamItemReader;
import com.likelion.backendplus4.talkpick.batch.chat.infrastructure.adapter.out.jpa.entity.ChatMessageEntity;
import com.likelion.backendplus4.talkpick.batch.chat.model.ChatMessage;
import com.likelion.backendplus4.talkpick.batch.chat.support.mapper.ChatMessageMapper;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class ChatBatchConfig {

	private final RedisStreamItemReader reader;
	private final ObjectMapper objectMapper;
	private final EntityManagerFactory emf;
	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	@Bean
	public JpaItemWriter<ChatMessageEntity> writer() {
		JpaItemWriter<ChatMessageEntity> writer = new JpaItemWriter<>();
		writer.setEntityManagerFactory(emf);
		writer.setUsePersist(true);
		return writer;
	}

	@Bean
	public Step chatFlushStep() {
		return new StepBuilder("chatFlushStep", jobRepository)
			.<MapRecord<String, String, String>, ChatMessageEntity>chunk(100, transactionManager)
			.reader(reader)
			.processor(record -> {
				ChatMessage domain = objectMapper.readValue(
					record.getValue().get("payload"),
					ChatMessage.class
				);
				return ChatMessageMapper.toEntityFromDomain(domain);
			})
			.writer(writer())
			.build();
	}

	@Bean
	public Job chatFlushJob() {
		return new JobBuilder("chatFlushJob", jobRepository)
			.incrementer(new RunIdIncrementer())
			.start(chatFlushStep())
			.build();
	}
}