package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.processor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.RssMappingFactory;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.RssMapper;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.parser.RssParserFactory;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.batch.RssSource;
import com.rometools.rome.feed.synd.SyndEntry;

@Component
@StepScope
public class RssEntryProcessor implements ItemProcessor<RssSource, List<ArticleEntity>> {

	private final RssParserFactory rssParserFactory;
	private final RssMappingFactory mappingFactory;

	public RssEntryProcessor(RssParserFactory rssParserFactory, RssMappingFactory mappingFactory) {
		this.rssParserFactory = rssParserFactory;
		this.mappingFactory = mappingFactory;
	}

	@Override
	public List<ArticleEntity> process(RssSource source) {
		List<ArticleEntity> result = new ArrayList<>();
		List<SyndEntry> entries = rssParserFactory.getFeed(source.getUrl());

		RssMapper mapper = mappingFactory.getMapper(source.getMapperType());
		for (SyndEntry entry : entries) {
			result.add(mapper.mapToRssNews(entry, source));
		}
		return result;
	}
}
