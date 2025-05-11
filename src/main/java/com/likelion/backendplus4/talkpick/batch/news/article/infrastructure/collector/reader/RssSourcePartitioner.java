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

@Slf4j
@Component
public class RssSourcePartitioner implements Partitioner {

	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {
		List<RssSource> sources = RssSource.getEnabledSources();
		System.out.println("sources = " + sources);
		int chunkSize = getCeil(sources, gridSize);
		log.info(String.valueOf(chunkSize));
		return createPartitionContextList(sources, chunkSize);
	}

	private int getCeil(List<RssSource> sources, int gridSize) {
		return (int)Math.ceil((double)sources.size() / gridSize);
	}

	private Map<String, ExecutionContext> createPartitionContextList(List<RssSource> sources, int chunkSize) {
		Map<String, ExecutionContext> partitions = new HashMap<>();

		int totalPartitions = (int)Math.ceil((double)sources.size() / chunkSize);

		for (int i = 0; i < totalPartitions; i++) {
			int from = i * chunkSize;
			int to = Math.min(from + chunkSize, sources.size());
			if (from >= to)
				break;

			List<RssSource> subList = new ArrayList<>(
				sources.subList(from, to).stream().toList()
			);
			ExecutionContext context = new ExecutionContext();
			context.put("sourceList", subList);
			partitions.put("partition" + i, context);
		}

		return partitions;
	}
}
