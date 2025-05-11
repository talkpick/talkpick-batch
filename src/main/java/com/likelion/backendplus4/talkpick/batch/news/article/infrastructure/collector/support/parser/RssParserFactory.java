package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.parser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

@Component
public class RssParserFactory {
	public List<SyndEntry> getFeed(String feedUrl) {
		URL url = getURL(feedUrl);
		try (XmlReader reader = new XmlReader(url)) {
			SyndFeedInput input = new SyndFeedInput();
			SyndFeed syndFeed = input.build(reader);
			return syndFeed.getEntries();
		} catch (Exception e) {
			throw new ArticleCollectorException(ArticleCollectorErrorCode.FEED_PARSING_ERROR);
		}
	}

	private URL getURL(String feedUrl) {
		try {
			return new URL(feedUrl);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
}
