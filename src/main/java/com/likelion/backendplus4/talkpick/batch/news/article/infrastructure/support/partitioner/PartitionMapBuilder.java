package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.support.partitioner;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.item.ExecutionContext;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.support.partitioner.dto.ArticleIdRange;

public class PartitionMapBuilder {
	public static Map<String, ExecutionContext> build(List<ArticleIdRange> ranges) {
		Map<String, ExecutionContext> partitions = new LinkedHashMap<>();
		for (int i = 0; i < ranges.size(); i++) {
			ArticleIdRange r = ranges.get(i);
			ExecutionContext ctx = new ExecutionContext();
			ctx.putLong("minId", r.start());
			ctx.putLong("maxId", r.end());
			partitions.put("partition" + i, ctx);
		}
		return partitions;
	}
}
