package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.batch;

import java.util.List;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;

/**
 * Spring Batch Step 구성 클래스.
 * RSS 기사 수집을 위한 병렬 처리용 파티셔닝 Step과
 * 실제 처리 로직이 포함된 청크 기반 Step을 설정한다.
 *
 * - articleRssPartitionedStep: 소스 데이터를 파티셔닝하여 병렬로 처리
 * - parseRssStep: 각 파티션 단위에서 RSS 데이터를 읽고, 가공 후 저장
 *
 * @since 2025-05-10
 */
@Configuration
public class StepConfig {
	private final String executorName = "normalExecutor";
	private static final String partitionedStepName = "articleRssPartitionedStep";
	private final String parseRssStepName = "parseRssStep";
	private final int gridSize = 3;
	private final int chunkSize = 10;
	private final int retryLimit = 3;
	private final int skipLimit = 100;
	private final JobRepository jobRepository;
	private final Partitioner rssSourcePartitioner;
	private final PlatformTransactionManager transactionManager;
	private final TaskExecutor taskExecutor;
	private final ItemProcessor<RssSource, List<ArticleEntity>> processor;
	private final ItemWriter<List<ArticleEntity>> writer;

	public StepConfig(JobRepository jobRepository,
		Partitioner rssSourcePartitioner,
		PlatformTransactionManager platformTransactionManager,
		@Qualifier(executorName)
		TaskExecutor taskExecutor,
		ItemProcessor<RssSource, List<ArticleEntity>> processor,
		ItemWriter<List<ArticleEntity>> writer) {
		this.jobRepository = jobRepository;
		this.rssSourcePartitioner = rssSourcePartitioner;
		this.transactionManager = platformTransactionManager;
		this.taskExecutor = taskExecutor;
		this.processor = processor;
		this.writer = writer;
	}

	/**
	 * RSS 소스 데이터를 파티셔닝하여 병렬로 처리하는 Step을 정의한다.
	 * 내부적으로 {@code parseRssStep}을 병렬 실행하며, TaskExecutor를 통해 스레드 분산 처리한다.
	 *
	 * @param parseRssStep 파티션마다 실행될 실제 처리 Step
	 * @return 파티셔닝 기반 Step
	 * @author 함예정
	 * @since 2025-05-10
	 */
	@Bean
	public Step articleRssPartitionedStep(Step parseRssStep) {
		return new StepBuilder(partitionedStepName, jobRepository)
			.partitioner(parseRssStep.getName(), rssSourcePartitioner)
			.step(parseRssStep)
			.taskExecutor(taskExecutor)
			.gridSize(gridSize)
			.build();
	}

	/**
	 * RSS 데이터를 청크 단위로 읽고, 가공하고, 저장하는 Step을 정의한다.
	 * 예외 발생 시 지정된 예외 클래스는 skip 처리되며, {@code skipLimit} 이하까지 허용된다.
	 *
	 * @param articleReader RSS 데이터 소스를 읽는 Reader
	 * @return RSS 처리용 Step
	 * @author 함예정
	 * @since 2025-05-10
	 */
	@Bean
	public Step parseRssStep(ItemReader<RssSource> articleReader) {
		return new StepBuilder(parseRssStepName, jobRepository)
			.<RssSource, List<ArticleEntity>>chunk(chunkSize, transactionManager)
			.reader(articleReader)
			.processor(processor)
			.writer(writer)
			.faultTolerant()
			.retry(ArticleCollectorException.class)
			.retryLimit(retryLimit)
			.skip(ArticleCollectorException.class)
			.skipLimit(skipLimit)
			.build();
	}
}
