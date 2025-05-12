package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.processor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

/**
 * RSS 피드 URL을 통해 XML 피드를 읽고 파싱하여 {@link SyndEntry} 목록으로 반환하는 Reader 클래스.
 * Rome 라이브러리를 이용하여 RSS를 파싱하며, 유효하지 않은 URL 또는 파싱 오류에 대해 예외를 처리한다.
 *
 * @since 2025-05-10
 */
@Component
public class RssFeedReader {

	/**
	 * 주어진 피드 URL로부터 RSS 피드를 파싱하고, {@link SyndEntry} 리스트를 반환한다.
	 *
	 * @param feedUrl RSS 피드의 URL 문자열
	 * @return 파싱된 SyndEntry 목록
	 * @throws ArticleCollectorException 피드 파싱에 실패한 경우
	 * @since 2025-05-10
	 * @author 함예정
	 */
	public List<SyndEntry> getFeed(String feedUrl) {
		URL url = getURL(feedUrl);
		try (XmlReader reader = new XmlReader(url)) {
			SyndFeedInput input = new SyndFeedInput();
			SyndFeed syndFeed = input.build(reader);
			return syndFeed.getEntries();
		} catch (Exception e) {
			throw new ArticleCollectorException(ArticleCollectorErrorCode.FEED_PARSING_ERROR, e);
		}
	}

	/**
	 * 문자열 형태의 URL을 {@link URL} 객체로 변환한다.
	 *
	 * @param feedUrl 문자열 형태의 URL
	 * @return URL 객체
	 * @throws RuntimeException 유효하지 않은 URL 형식일 경우
	 * @since 2025-05-10
	 * @author 함예정
	 */
	private URL getURL(String feedUrl) {
		try {
			return new URL(feedUrl);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	private URLConnection openConnectionWithTimeout(URL url) {
		try {
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(3000);
			connection.setReadTimeout(5000);
			return connection;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}


	}
}
