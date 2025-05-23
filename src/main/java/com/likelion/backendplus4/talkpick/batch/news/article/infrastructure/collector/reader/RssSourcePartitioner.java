package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.batch.RssSource;

/**
 * 활성화된 RSS 소스를 파티션 단위로 분할하여 StepExecutionContext에 전달하는 Partitioner 구현체.
 * Spring Batch에서 멀티 스레드/병렬 실행을 위해 사용된다.
 * <p>
 * 각 파티션은 sourceList를 포함한 ExecutionContext로 구성된다.
 *
 * @since 2025-05-10
 */
@Component
public class RssSourcePartitioner implements Partitioner {
    /**
     * 전체 RSS 소스를 파티셔닝하여 각 파티션별 ExecutionContext를 생성한다.
     * 모든 활성화된 RSS 소스(카테고리 포함)를 처리한다.
     *
     * @param gridSize 실행할 파티션 수
     * @return 파티션 이름과 ExecutionContext의 매핑 정보
     * @modified 2025-05-14 모든 카테고리 처리하도록 수정
     * @author 함예정
     * @since 2025-05-10
     */
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        List<RssSource> allSources = RssSource.getEnabledSources();

        int chunkSize = calculateChunkSize(allSources.size(), gridSize);
        return buildPartitions(allSources, chunkSize);
    }

    /**
     * 총 소스 수와 파티션 수를 기반으로 파티션당 소스 개수를 계산한다.
     *
     * @param totalSources 전체 RSS 소스 수
     * @param gridSize     파티션 수
     * @return 파티션당 소스 개수
     * @since 2025-05-10
     */
    private int calculateChunkSize(int totalSources, int gridSize) {
        int chunkSize = (int) Math.ceil((double) totalSources / gridSize);
        return chunkSize;
    }

    /**
     * RSS 소스를 주어진 chunkSize로 나눠 각 파티션별 ExecutionContext를 생성한다.
     *
     * @param sources   RSS 소스 리스트
     * @param chunkSize 파티션당 소스 개수
     * @return 파티션 맵
     * @since 2025-05-10
     */
    private Map<String, ExecutionContext> buildPartitions(List<RssSource> sources, int chunkSize) {
        Map<String, ExecutionContext> partitions = new HashMap<>();
        int totalPartitions = calculateTotalPartitions(sources, chunkSize);

        for (int i = 0; i < totalPartitions; i++) {
            int from = i * chunkSize;
            int to = calculateChunkEndIndex(sources, chunkSize, from);

            if (from >= to) {
                break;
            }

            ExecutionContext context = buildExecutionContext(sources, from, to);
            partitions.put("partition" + i, context);
        }

        return partitions;
    }

    /**
     * 주어진 RSS 소스 리스트를 청크 크기(chunkSize)로 분할할 때 필요한 총 파티션 수를 계산합니다.
     *
     * @param sources   RSS 소스 목록
     * @param chunkSize 하나의 파티션에 포함될 RSS 소스 수
     * @return 전체 파티션 수
     * @since 2025-05-12
     */
    private int calculateTotalPartitions(List<RssSource> sources, int chunkSize) {
        return (sources.size() + chunkSize - 1) / chunkSize;
    }

    /**
     * 주어진 시작 인덱스(from)와 청크 크기(chunkSize)를 기반으로,
     * 리스트의 범위를 초과하지 않도록 제한된 끝 인덱스를 계산합니다.
     *
     * @param sources   RSS 소스 리스트
     * @param chunkSize 하나의 파티션에 포함될 RSS 소스 수
     * @param from      시작 인덱스
     * @return 리스트 범위를 초과하지 않는 끝 인덱스
     * @since 2025-05-12
     */
    private int calculateChunkEndIndex(List<RssSource> sources, int chunkSize, int from) {
        return Math.min(from + chunkSize, sources.size());
    }

    /**
     * 지정된 인덱스 범위에 해당하는 RSS 소스 부분 리스트로 ExecutionContext를 생성한다.
     * 생성된 context는 Spring Batch 파티션 실행 시 각 Step에 전달된다.
     *
     * @param sources 전체 RSS 소스 리스트
     * @param from    시작 인덱스 (포함)
     * @param to      종료 인덱스 (미포함)
     * @return 파티션별 RSS 소스가 포함된 ExecutionContext
     * @since 2025-05-10
     */
    private ExecutionContext buildExecutionContext(List<RssSource> sources, int from, int to) {
        List<RssSource> subList = new ArrayList<>(sources.subList(from, to));
        ExecutionContext context = new ExecutionContext();
        context.put("sourceList", subList);
        return context;
    }
}