package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.support.partitioner;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.support.partitioner.dto.ArticleIdRange;

@Component
public class IdRangePartitionCalculator {

	/**
	 * @param minId    최소 ID
	 * @param maxId    최대 ID
	 * @param gridSize 분할 개수
	 * @return 각 파티션의 IdRange 리스트
	 */
	public List<ArticleIdRange> calculate(long minId, long maxId, int gridSize) {
		long total     = maxId - minId + 1;
		long baseSize  = total / gridSize;
		long remainder = total % gridSize;

		List<ArticleIdRange> ranges = new ArrayList<>(gridSize);
		long start = minId;

		for (int i = 0; i < gridSize; i++) {
			long size = baseSize + (i < remainder ? 1 : 0);
			long end  = (i == gridSize - 1) ? maxId : (start + size - 1);

			ranges.add(new ArticleIdRange(start, end));
			start = end + 1;
		}

		return ranges;
	}
}