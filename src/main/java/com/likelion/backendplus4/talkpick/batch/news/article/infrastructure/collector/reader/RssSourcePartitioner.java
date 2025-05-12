package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.batch.RssSource;

import lombok.extern.slf4j.Slf4j;

/**
 * 활성화된 RSS 소스를 파티션 단위로 분할하여 StepExecutionContext에 전달하는 Partitioner 구현체.
 * Spring Batch에서 멀티 스레드/병렬 실행을 위해 사용된다.
 *
 * 각 파티션은 sourceList를 포함한 ExecutionContext로 구성된다.
 *
 * @since 2025-05-10
 */
@Slf4j
@Component
public class RssSourcePartitioner implements Partitioner {

	/**
	 * 전체 RSS 소스를 파티셔닝하여 각 파티션별 ExecutionContext를 생성한다.
	 *
	 * @param gridSize 실행할 파티션 수
	 * @return 파티션 이름과 ExecutionContext의 매핑 정보
	 * @since 2025-05-10
	 * @author 함예정
	 */
	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {
		List<RssSource> sources = RssSource.getEnabledSources();
		int chunkSize = calculateChunkSize(sources.size(), gridSize);
		return buildPartitions(sources, chunkSize);
	}

	/**
	 * 총 소스 수와 파티션 수를 기반으로 파티션당 소스 개수를 계산한다.
	 *
	 * @param totalSources 전체 RSS 소스 수
	 * @param gridSize 파티션 수
	 * @return 파티션당 소스 개수
	 * @since 2025-05-10
	 * @author 함예정
	 */
	private int calculateChunkSize(int totalSources, int gridSize) {
		int chunkSize = (int)Math.ceil((double)totalSources / gridSize);
		log.info("Calculated chunkSize: {}", chunkSize);
		return chunkSize;
	}

	/**
	 * RSS 소스를 주어진 chunkSize로 나눠 각 파티션별 ExecutionContext를 생성한다.
	 *
	 * @param sources RSS 소스 리스트
	 * @param chunkSize 파티션당 소스 개수
	 * @return 파티션 맵
	 * @since 2025-05-10
	 * @author 함예정
	 */
	private Map<String, ExecutionContext> buildPartitions(List<RssSource> sources, int chunkSize) {
		Map<String, ExecutionContext> partitions = new HashMap<>();
		int totalPartitions = (int)Math.ceil((double)sources.size() / chunkSize);

		for (int i = 0; i < totalPartitions; i++) {
			int from = i * chunkSize;
			int to = Math.min(from + chunkSize, sources.size());

			if (from >= to) {
				break;
			}

			ExecutionContext context = buildExecutionContext(sources, from, to);
			partitions.put("partition" + i, context);
		}

		return partitions;
	}

	/**
	 * 지정된 인덱스 범위에 해당하는 RSS 소스 부분 리스트로 ExecutionContext를 생성한다.
	 * 생성된 context는 Spring Batch 파티션 실행 시 각 Step에 전달된다.
	 *
	 * @param sources 전체 RSS 소스 리스트
	 * @param from 시작 인덱스 (포함)
	 * @param to 종료 인덱스 (미포함)
	 * @return 파티션별 RSS 소스가 포함된 ExecutionContext
	 * @since 2025-05-10
	 * @author 함예정
	 */
	private ExecutionContext buildExecutionContext(List<RssSource> sources, int from, int to) {
		List<RssSource> subList = new ArrayList<>(sources.subList(from, to));
		ExecutionContext context = new ExecutionContext();
		context.put("sourceList", subList);
		return context;
	}
}
