package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.reader;

import java.util.Iterator;
import java.util.List;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.rss.RssSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@StepScope
public class ArticleReader implements ItemReader<RssSource> {

	private final Iterator<RssSource> iterator;

	public ArticleReader(@Value("#{stepExecutionContext['sourceList']}") List<RssSource> sources) {
		this.iterator = sources.iterator();
	}

	@Override
	public RssSource read() {
		return iterator.hasNext() ? iterator.next() : null;
	}
}
