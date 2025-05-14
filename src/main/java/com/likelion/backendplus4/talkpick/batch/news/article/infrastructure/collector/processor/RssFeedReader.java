package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.processor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.repository.RssNewsRepository;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import lombok.extern.slf4j.Slf4j;

/**
 * RSS 피드 URL을 통해 XML 피드를 읽고 파싱하여 {@link SyndEntry} 목록으로 반환하는 Reader 클래스.
 * Rome 라이브러리를 이용하여 RSS를 파싱하며, 유효하지 않은 URL 또는 파싱 오류에 대해 예외를 처리한다.
 *
 * @since 2025-05-10
 * @modified 2025-05-18 최신 발행일 이후 데이터만 필터링하는 기능 추가
 */
@Slf4j
@Component
public class RssFeedReader {
	private final RssNewsRepository rssNewsRepository;
	private static final Map<String, LocalDateTime> lastProcessedDateMap = new ConcurrentHashMap<>();

	@Autowired
	public RssFeedReader(RssNewsRepository rssNewsRepository) {
		this.rssNewsRepository = rssNewsRepository;
	}

	/**
	 * 주어진 피드 URL로부터 RSS 피드를 파싱하고, 최신 발행일 이후의 {@link SyndEntry} 리스트를 반환한다.
	 *
	 * @param feedUrl RSS 피드의 URL 문자열
	 * @param mapperType 매퍼 타입 (언론사 코드)
	 * @return 파싱 및 필터링된 SyndEntry 목록
	 * @since 2025-05-10
	 * @modified 2025-05-18 최신 발행일 이후 데이터만 필터링하는 기능 추가
	 * @author 함예정
	 */
	public List<SyndEntry> getFeed(String feedUrl, String mapperType) {
		URL url = getURL(feedUrl);
		URLConnection connection = openConnectionWithTimeout(url);
		List<SyndEntry> entries = parseRssEntries(connection);

		LocalDateTime latestPubDate = getLatestPubDate(mapperType);

		List<SyndEntry> filteredEntries = entries.stream()
				.filter(entry -> isAfterLatestPubDate(entry, latestPubDate))
				.collect(Collectors.toList());

		return filteredEntries;
	}

	/**
	 * 언론사별 최신 발행일 조회 (캐싱 추가)
	 *
	 * @param mapperType 매퍼 타입 (언론사 코드)
	 * @return 최신 발행일 또는 기본값
	 */
	private LocalDateTime getLatestPubDate(String mapperType) {
		String guidPrefix = mapperType.toUpperCase();

		LocalDateTime latestPubDate = rssNewsRepository.findLatestPubDateByGuidPrefix(guidPrefix);

		if (latestPubDate == null) {
			latestPubDate = LocalDateTime.now().minusDays(1);
		}

		lastProcessedDateMap.put(mapperType, latestPubDate);
		return latestPubDate;
	}

	/**
	 * 항목의 발행일이 최신 발행일보다 이후인지 확인
	 *
	 * @param entry RSS 항목
	 * @param latestPubDate 최신 발행일
	 * @return 최신 발행일 이후면 true
	 */
	private boolean isAfterLatestPubDate(SyndEntry entry, LocalDateTime latestPubDate) {
		if (entry.getPublishedDate() == null) {
			log.debug("발행일 없음 - 항목 제외: {}", entry.getTitle());
			return false;
		}

		LocalDateTime pubDate = convertToLocalDateTime(entry.getPublishedDate());

		boolean isAfter = pubDate.isAfter(latestPubDate);

		return isAfter;
	}

	/**
	 * Date 객체를 LocalDateTime으로 변환
	 *
	 * @param date 변환할 Date 객체
	 * @return 변환된 LocalDateTime
	 */
	private LocalDateTime convertToLocalDateTime(Date date) {
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
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

	/**
	 * 지정된 URL에 대해 연결 타임아웃과 읽기 타임아웃을 설정한 후 URLConnection을 반환합니다.
	 *
	 * @param url 연결할 URL 객체
	 * @return 설정된 타임아웃을 가진 URLConnection 객체
	 * @throws RuntimeException 연결 중 IOException이 발생할 경우 런타임 예외로 래핑하여 던짐
	 * @author 함예정
	 * @since 2025-05-12
	 */
	private URLConnection openConnectionWithTimeout(URL url) {
		try {
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(3000);
			connection.setReadTimeout(5000);
			return connection;
		} catch (IOException e) {
			throw new ArticleCollectorException(ArticleCollectorErrorCode.FEED_CONNECTION_ERROR, e);
		}
	}

	/**
	 * 주어진 URLConnection으로부터 RSS 피드를 읽어 SyndEntry 목록으로 파싱합니다.
	 *
	 * @param connection RSS 피드를 제공하는 URLConnection 객체
	 * @return 파싱된 SyndEntry 객체 리스트
	 * @throws ArticleCollectorException RSS 피드 파싱 중 오류가 발생한 경우 사용자 정의 예외로 래핑하여 던짐
	 * @author 함예정
	 * @since 2025-05-12
	 */
	private List<SyndEntry> parseRssEntries(URLConnection connection) {
		try (XmlReader reader = new XmlReader(connection)) {
			SyndFeedInput input = new SyndFeedInput();
			SyndFeed syndFeed = input.build(reader);
			return syndFeed.getEntries();
		} catch (Exception e) {
			throw new ArticleCollectorException(ArticleCollectorErrorCode.FEED_PARSING_ERROR, e);
		}
	}
}