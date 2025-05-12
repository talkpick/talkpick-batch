package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.processor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.factory.RssMappingFactory;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.batch.RssSource;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.AbstractRssMapper;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;
import com.rometools.rome.feed.synd.SyndEntry;

/**
 * RSS 소스를 기반으로 기사 목록을 생성하는 Spring Batch ItemProcessor 구현체.
 * 주어진 {@link RssSource}의 URL로부터 SyndEntry 목록을 파싱하고,
 * 매핑 전략에 따라 {@link ArticleEntity} 리스트로 변환한다.
 *
 * - RSS 파싱: {@link RssFeedReader}를 통해 피드를 읽어옴
 * - 데이터 매핑: {@link RssMappingFactory}에서 소스 타입에 따라 매퍼 선택
 *
 * 이 클래스는 Step 실행 시에만 생성되며, StepScope에 따라 각 파티션마다 독립적으로 주입된다.
 *
 * @since 2025-05-10
 * @modified 2025-05-13 RssMapper to AbstractRssMapper로 변경
 */
@Component
@StepScope
public class RssEntryProcessor implements ItemProcessor<RssSource, List<ArticleEntity>> {

	private final RssFeedReader rssFeedReader;
	private final RssMappingFactory mappingFactory;

	public RssEntryProcessor(RssFeedReader rssFeedReader, RssMappingFactory mappingFactory) {
		this.rssFeedReader = rssFeedReader;
		this.mappingFactory = mappingFactory;
	}

	/**
	 * 단일 RSS 소스를 받아 파싱 후 기사 리스트로 변환한다.
	 * - RSS 파싱
	 * - 매핑 전략 선택
	 * - 기사 리스트 생성
	 *
	 * @param source RSS 피드 소스 정보
	 * @return 해당 소스에서 추출된 기사 엔티티 리스트
	 * @author 함예정
	 * @since 2025-05-10
	 */
	@Override
	public List<ArticleEntity> process(RssSource source) {
		List<SyndEntry> rssParseResult = parseRss(source);
		AbstractRssMapper mapper = getMapper(source);
		return buildArticleEntityList(source, rssParseResult, mapper);
	}

	/**
	 * RSS 소스의 URL을 기반으로 피드를 파싱하여 SyndEntry 리스트를 반환한다.
	 *
	 * @param source RSS 피드 소스
	 * @return 파싱된 RSS 엔트리 리스트
	 * @since 2025-05-10
	 * @author 함예정
	 */
	private List<SyndEntry> parseRss(RssSource source) {
		return rssFeedReader.getFeed(source.getUrl());
	}

	/**
	 * RSS 소스의 매퍼 타입에 따라 적절한 매퍼를 반환한다.
	 *
	 * @param source 매핑 전략이 포함된 RSS 소스
	 * @return 매퍼 인스턴스
	 * @since 2025-05-10
	 * @author 함예정
	 * @modified 2025-05-13 AbstractRssMapper 타입으로 변경
	 */
	private AbstractRssMapper getMapper(RssSource source) {
		return mappingFactory.getMapper(source.getMapperType());
	}

	/**
	 * SyndEntry RSS 결과를 기반으로 ArticleEntity 리스트를 생성한다.
	 *
	 * @param source RSS 소스 정보
	 * @param rssParseResult RSS 피드에서 파싱된 엔트리 리스트
	 * @param mapper 소스에 맞는 RSS 매퍼
	 * @return 변환된 ArticleEntity 리스트
	 * @since 2025-05-10
	 * @author 함예정
	 * @modified 2025-05-13 AbstractRssMapper 타입으로 변경
	 */
	private List<ArticleEntity> buildArticleEntityList(RssSource source, List<SyndEntry> rssParseResult,
													   AbstractRssMapper mapper) {
		List<ArticleEntity> result = new ArrayList<>();
		for (SyndEntry entry : rssParseResult) {
			result.add(mapper.mapToRssNews(entry, source));
		}
		return result;
	}
}