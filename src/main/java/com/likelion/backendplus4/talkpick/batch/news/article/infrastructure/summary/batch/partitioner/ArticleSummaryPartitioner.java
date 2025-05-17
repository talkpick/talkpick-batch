package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.summary.batch.partitioner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import lombok.Setter;

/**
 * {@link Partitioner} 구현체로, 전체 페이지 수(totalPages)를 기준으로
 * 병렬 처리할 수 있도록 페이지 범위를 gridSize만큼 균등하게 분할한다.
 *
 * <p>Spring Batch에서 멀티스레드로 작업을 병렬 처리할 때 사용되며,
 * 각 ExecutionContext에는 'startPage'와 'endPage'가 설정된다.
 *
 * @since 2025-05-17
 */
@Component
@Setter
public class ArticleSummaryPartitioner implements Partitioner {
	private int totalPages = 0;

	/**
	 * 단순한 페이지 범위 DTO로, 시작 페이지(start)와 끝 페이지(end)를 저장한다.
	 *
	 * @since 2025-05-17
	 */
	private static class PageRange {
		final int start, end;

		PageRange(int start, int end) {
			this.start = start;
			this.end = end;
		}
	}
	/**
	 * 지정된 gridSize에 따라 totalPages를 균등하게 분할한 ExecutionContext 맵을 반환한다.
	 * 각 컨텍스트는 startPage와 endPage 값을 포함한다.
	 *
	 * @param gridSize 생성할 파티션 수
	 * @return 파티션 이름(key)과 해당 페이지 범위 컨텍스트(value)의 맵
	 * @author 함예정
	 * @since 2025-05-17
	 */
	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {
		int partitionCount = Math.min(gridSize, totalPages);
		List<PageRange> ranges = calculateRanges(totalPages, partitionCount);
		return createExecutionContextMap(ranges);
	}

	/**
	 * 페이지 범위 리스트를 기반으로 각 파티션에 대한 ExecutionContext 맵을 생성한다.
	 *
	 * @param ranges 페이지 범위 리스트
	 * @return 파티션 이름과 ExecutionContext가 매핑된 맵
	 * @author 함예정
	 * @since 2025-05-17
	 */
	private Map<String, ExecutionContext> createExecutionContextMap(List<PageRange> ranges) {
		Map<String, ExecutionContext> map = new LinkedHashMap<>(ranges.size());
		for (int i = 0; i < ranges.size(); i++) {
			PageRange r = ranges.get(i);
			ExecutionContext ctx = new ExecutionContext();
			ctx.putInt("startPage", r.start);
			ctx.putInt("endPage", r.end);
			map.put("partition" + i, ctx);
		}
		return map;
	}


	/**
	 * totalPages를 partitionCount 개수로 균등 분할하여 각 파티션에 해당하는
	 * 페이지 범위(start ~ end)를 리스트로 반환한다.
	 *
	 * @param totalPages 전체 페이지 수
	 * @param partitionCount 분할할 파티션 수
	 * @return 파티션당 페이지 범위를 나타내는 PageRange 리스트
	 * @author 함예정
	 * @since 2025-05-17
	 */
	private List<PageRange> calculateRanges(int totalPages, int partitionCount) {
		int baseSize = totalPages / partitionCount;
		int remainder = totalPages % partitionCount;
		return generatePageRanges(baseSize, remainder, partitionCount);
	}

	/**
	 * 주어진 baseSize와 remainder를 이용하여 파티션 개수만큼 페이지 범위를 생성한다.
	 *
	 * @param baseSize 각 파티션의 기본 크기
	 * @param remainder 나머지 페이지 수 (앞에서부터 1개씩 분배)
	 * @param partitionCount 파티션 수
	 * @return 생성된 PageRange 리스트
	 * @author 함예정
	 * @since 2025-05-17
	 */
	private List<PageRange> generatePageRanges(int baseSize, int remainder, int partitionCount) {
		List<PageRange> list = new ArrayList<>(partitionCount);
		int cursor = 1;
		for (int i = 0; i < partitionCount; i++) {
			int extra = (i < remainder ? 1 : 0);
			int size  = baseSize + extra;
			list.add(new PageRange(cursor, cursor + size - 1));
			cursor += size;
		}
		return list;
	}
}