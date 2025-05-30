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

import com.likelion.backendplus4.talkpick.batch.chat.exception.ChatBatchException;
import com.likelion.backendplus4.talkpick.batch.chat.infrastructure.adapter.out.batch.listener.RedisAckListener;
import com.likelion.backendplus4.talkpick.batch.chat.infrastructure.adapter.out.batch.processor.ChatMessageItemProcessor;
import com.likelion.backendplus4.talkpick.batch.chat.infrastructure.adapter.out.batch.reader.RedisStreamItemReader;
import com.likelion.backendplus4.talkpick.batch.chat.infrastructure.adapter.out.jpa.entity.ChatMessageEntity;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;

/**
 * Redis 스트림으로부터 채팅 메시지를 읽어와 JPA로 영속화하는 배치 잡을 설정하는 구성 클래스입니다.
 *
 * @since 2025-05-27
 */
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class ChatBatchConfig {

    private static final String STEP_NAME = "chatFlushStep";
    private static final String JOB_NAME = "chatFlushJob";
	private static final int RETRY_COUNT = 3;
	private static final int SKIP_COUNT = 10;

	private final RedisStreamItemReader reader;
	private final RedisAckListener redisAckListener;
	private final EntityManagerFactory emf;
	private final JobRepository jobRepository;
	private final ChatMessageItemProcessor processor;
	private final PlatformTransactionManager transactionManager;

	/**
	 * Redis 스트림에서 읽은 메시지를 처리하여 DB에 저장하는 Step을 생성합니다.
	 *
	 * @return 채팅 플러시를 수행하는 Step 객체
	 * @author 박찬병
	 * @since 2025-05-27
	 */
	@Bean
	public Step chatFlushStep() {
		return new StepBuilder(STEP_NAME, jobRepository)
			.<MapRecord<String, String, String>, ChatMessageEntity>chunk(100, transactionManager)
			.reader(reader)
			.processor(processor)
			.writer(writer())
			.listener(redisAckListener)
			.faultTolerant()
			.retryLimit(RETRY_COUNT)
			.retry(ChatBatchException.class)
			.skipLimit(SKIP_COUNT)
			.skip(ChatBatchException.class)
			.build();
	}

	/**
	 * chatFlushStep을 실행하는 배치 Job을 생성합니다.
	 *
	 * @return 채팅 플러시 배치 Job 객체
	 * @author 박찬병
	 * @since 2025-05-27
	 */
	@Bean
	public Job chatFlushJob() {
		return new JobBuilder(JOB_NAME, jobRepository)
			.incrementer(new RunIdIncrementer())
			.start(chatFlushStep())
			.build();
	}

	/**
	 * ChatMessageEntity를 저장하기 위한 JpaItemWriter 빈을 정의합니다.
	 *
	 * @return JpaItemWriter<ChatMessageEntity> 객체
	 * @author 박찬병
	 * @since 2025-05-27
	 */
	@Bean
	public JpaItemWriter<ChatMessageEntity> writer() {
		JpaItemWriter<ChatMessageEntity> writer = new JpaItemWriter<>();
		writer.setEntityManagerFactory(emf);
		writer.setUsePersist(true);
		return writer;
	}

}