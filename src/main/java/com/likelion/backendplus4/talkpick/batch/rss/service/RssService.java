package com.likelion.backendplus4.talkpick.batch.rss.service;

import com.likelion.backendplus4.talkpick.batch.rss.entity.RssNews;
import com.likelion.backendplus4.talkpick.batch.rss.model.RssSource;
import com.likelion.backendplus4.talkpick.batch.rss.repository.RssNewsRepository;
import com.likelion.backendplus4.talkpick.batch.rss.service.mapper.RssMapper;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

/**
 * RSS 피드를 수집하고 처리하는 서비스 클래스
 *
 * @author 양병학
 * @since 2025-05-10 최초 작성
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RssService {

    private final RssNewsRepository rssNewsRepository;
    private final RssMappingFactory rssMappingFactory;

    /**
     * 모든 활성화된 RSS 뉴스 피드를 가져와 처리
     *
     * 1. 활성화된 모든 RSS 소스 조회
     * 2. 각 소스에서 RSS 피드 가져오기
     * 3. 수집된 모든 뉴스 항목 병합
     * 4. 데이터베이스에 저장 (중복 제외)
     *
     * @return 새로 저장된 뉴스 항목 수
     * @since 2025-05-10 최초 작성
     * @author 양병학
     */
    @Transactional
    public int fetchAndProcessAllFeeds() {
        List<RssNews> allNewsItems = new ArrayList<>();

        RssSource.getEnabledSources().forEach(source -> {
            try {
                List<RssNews> items = fetchFeedFromSource(source);
                allNewsItems.addAll(items);
            } catch (Exception e) {
                log.error("Error: 뉴스피드 fetch중 {}-{}: {}",
                        source.getPublisherName(), source.getCategoryName(),
                        e.getMessage(), e);
            }
        });

        return saveItems(allNewsItems);
    }

    /**
     * 특정 소스에서 RSS 피드를 가져옴
     *
     * @param source RSS 소스 정보
     * @return 변환된 RssNews 엔티티 목록
     * @throws Exception RSS 피드 로드 및 처리 중 발생할 수 있는 예외
     */
    private List<RssNews> fetchFeedFromSource(RssSource source) throws Exception {
        URL feedUrl = new URL(source.getUrl());
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(feedUrl));

        List<RssNews> newsItems = new ArrayList<>();
        RssMapper mapper = rssMappingFactory.getMapper(source.getMapperType());

        for (SyndEntry entry : feed.getEntries()) {
            RssNews newsItem = mapper.mapToRssNews(entry, source);
            newsItems.add(newsItem);
        }

        log.info("페치완료 {} 갯수 뉴스피드 {}-{} 에서",
                newsItems.size(), source.getPublisherName(), source.getCategoryName());
        return newsItems;
    }

    /**
     * 수집된 뉴스 항목을 DB에 저장
     *
     * @param newsItems 저장할 뉴스 항목 목록
     * @return 새로 저장된 항목 수
     */
    @Transactional
    public int saveItems(List<RssNews> newsItems) {
        int savedCount = 0;

        List<RssNews> newItems = newsItems.stream()
                .filter(not(item -> rssNewsRepository.existsByLink(item.getLink())))
                .collect(Collectors.toList());

        for (RssNews item : newItems) {
            try {
                rssNewsRepository.save(item);
                savedCount++;
            } catch (DataIntegrityViolationException e) {
                log.debug("중복 항목 감지: {}", item.getLink());
            }
        }

        log.info("{}개 뉴스 저장완료 (새로운 항목: {}, 총 가져온 항목: {})",
                savedCount, newItems.size(), newsItems.size());
        return savedCount;
    }
}