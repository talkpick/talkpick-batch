package com.likelion.backendplus4.talkpick.batch.rss.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.likelion.backendplus4.talkpick.batch.rss.config.RssSourceConfig;
import com.likelion.backendplus4.talkpick.batch.rss.entity.RssNews;
import com.likelion.backendplus4.talkpick.batch.rss.repository.RssNewsRepository;
import com.likelion.backendplus4.talkpick.batch.rss.service.mapper.RssMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

/**
 * RSS 피드를 수집하고 처리하는 서비스 클래스
 *
 * @author 양병학
 * @since 2024-05-10 최초 작성
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RssService {

    private final RssSourceConfig rssSourceConfig;
    private final RssNewsRepository rssNewsRepository;
    private final RssMappingFactory rssMappingFactory;

    /** 모든 활성화된 RSS 뉴스 피드를 가져와 처리 (서비스 계층 메인기능)
    * 1. 설정에서 활성화된 모든 RSS 소스 조회
    * 2. 각 소스에서 RSS 피드 가져오기
    * 3. 수집된 모든 뉴스 항목 병합
    * 4. 데이터베이스에 저장 (중복 제외)
    *
    * @return 새로 저장된 뉴스 항목 수
    * @throws Exception RSS 처리 중 발생할 수 있는 예외
    * @since 2025-05-10 최초 작성
    * @author 양병학
    * @modify 2025-05-10 양병학
     **/
    @Transactional
    public int fetchAndProcessAllFeeds() {
        List<RssNews> allNewsItems = new ArrayList<>();

        rssSourceConfig.getSources().stream()
                .filter(RssSourceConfig.RssSource::isEnabled)
                .forEach(source -> {
                    try {
                        List<RssNews> items = fetchFeedFromSource(source);
                        allNewsItems.addAll(items);
                    } catch (Exception e) {
                        log.error("Error: 뉴스피드 fetch중 {}: {}", source.getName(), e.getMessage(), e);
                    }
                });

        return saveItems(allNewsItems);
    }

    /**
     * 특정 소스에서 RSS 피드를 가져옴
     *
     * 1. 소스 URL(application-dev)에서 뉴스 피드 로드 (dev파일은 디스코드 스레드에 올리겠습니다)
     * 2. 소스 유형에 맞는 Mapper(server/mapper) 가져오기
     * 3. 각 항목을 RssNews 엔티티로 변환
     *
     * @param source RSS 피드 소스 설정
     * @return 변환된 RssNews 엔티티 목록
     * @throws Exception RSS 피드 로드 및 처리 중 발생할 수 있는 예외
     * @since 2024-05-10 최초 작성
     * @author 양병학
     */
    private List<RssNews> fetchFeedFromSource(RssSourceConfig.RssSource source) throws Exception {
        URL feedUrl = new URL(source.getUrl());
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(feedUrl));

        List<RssNews> newsItems = new ArrayList<>();
        RssMapper mapper = rssMappingFactory.getMapper(source.getType());

        for (SyndEntry entry : feed.getEntries()) {
            RssNews newsItem = mapper.mapToRssNews(entry);
            newsItems.add(newsItem);
        }

        log.info("fetch 완료 {} 갯수 뉴스피드 {} 에서", newsItems.size(), source.getName());
        return newsItems;
    }

    /**
     * 수집된 뉴스 항목을 DB에 저장합니다.(추후 Batch로 다시구현하면 없어질 메서드)
     * 중복된 항목은 건너뛰고 새로운 항목만 저장. (INSERT IGNORE로 대체시 Native Query 필요, 수정사항 처리 메서드 아직 없음)
     *
     * 1. 기존 존재하는 링크 필터링
     * 2. 새 항목만 데이터베이스에 저장
     * 3. 중복 저장 시도 시 예외 처리
     *
     * @param newsItems 저장할 뉴스 항목 목록
     * @return 새로 저장된 항목 수
     * @since 2024-05-10 최초 작성
     * @author 양병학
     * @modify 2024-05-10 양병학
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