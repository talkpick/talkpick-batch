package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.implement;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.batch.RssSource;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.AbstractRssMapper;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.ContentScraper;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.scraper.factory.ScraperFactory;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;
import com.rometools.rome.feed.synd.SyndEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 경향신문 RSS 매퍼 구현체
 * ContentScraper를 사용하여 기사 본문 스크래핑
 *
 * @author 양병학
 * @since 2025-05-10 최초 작성
 * @modified 2025-05-15 템플릿 메서드 패턴 적용, 의존성 주입 방식 개선
 * @modified 2025-05-17 스크래핑 로직 추가 및 불용어 제거 기능 추가
 */
@Component
public class KhanRssMapper extends AbstractRssMapper {

    private static final Logger log = LoggerFactory.getLogger(KhanRssMapper.class);
//    private static final Map<String, ArticleEntity> RESULT_CACHE = new ConcurrentHashMap<>();
    private final ScraperFactory scraperFactory;

    @Autowired
    public KhanRssMapper(ScraperFactory scraperFactory) {
        this.scraperFactory = scraperFactory;
    }

    /**
     * RSS 피드를 ArticleEntity 엔티티로 변환
     * ContentScraper를 사용하여 기사 본문 스크래핑
     *
     * @param entry 변환할 SyndEntry(Rss 데이터) 객체
     * @param source RSS 소스 정보
     * @return 변환된 ArticleEntity 엔티티
     * @since 2025-05-10
     * @modified 2025-05-17 스크래핑 로직 추가
     */
    @Override
    public ArticleEntity mapToRssNews(SyndEntry entry, RssSource source) {
        String guid = extractGuid(entry, source);

        String title = extractTitle(entry);
        String link = extractLink(entry);
        LocalDateTime pubDate = extractPubDate(entry);
        String category = extractCategory(entry, source);
        String imageUrl = extractImageUrl(entry);
        String description = "";

        try {
            ContentScraper scraper = scraperFactory.getScraper("kh")
                    .orElseThrow(() -> new ArticleCollectorException(ArticleCollectorErrorCode.MAPPER_NOT_FOUND));

            String scrapedContent = scraper.scrapeContent(link);
            if (scrapedContent != null && !scrapedContent.isEmpty()) {
                description = removeUnwantedPhrases(scrapedContent);
            }

            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = scraper.scrapeImageUrl(link);
            }
        } catch (Exception e) {
            log.error("경향일보 스크래핑 실패: {}", e.getMessage());
        }

        ArticleEntity article = ArticleEntity.builder()
                .title(title)
                .link(link)
                .pubDate(pubDate)
                .category(category)
                .guid(guid)
                .description(description)
                .imageUrl(imageUrl)
                .build();

        return article;
    }



    /**
     * 템플릿 메서드 패턴에서 사용할 ScraperFactory 반환
     *
     * @return 주입받은 ScraperFactory 인스턴스
     * @since 2025-05-15
     */
    @Override
    protected ScraperFactory getScraperFactory() {
        return this.scraperFactory;
    }

    /**
     * 불용어 제거 메서드
     * 저작권 문구, 광고 문구 등 불필요한 문구 제거
     *
     * @param content 원본 내용
     * @return 불용어가 제거된 내용
     * @since 2025-05-17
     */
    private String removeUnwantedPhrases(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        content = content.replaceAll("\\(c\\)\\s*경향신문", "");
        content = content.replaceAll("저작권자.*경향신문.*무단.*전재.*금지", "");
        content = content.replaceAll("무단전재 및 재배포 금지", "");
        content = content.replaceAll("\\S+기자\\s+\\S+@khan\\.co\\.kr", "");
        content = content.replaceAll("경향신문 뉴스스탠드", "");
        content = content.replaceAll("경향닷컴", "");
        content = content.replaceAll("PARAGRAPH_BREAKPARAGRAPH_BREAK", "PARAGRAPH_BREAK");

        return content.trim();
    }

    /**
     * 매퍼 타입 반환
     *
     * @return 매퍼 타입 (kh)
     * @since 2025-05-10
     */
    @Override
    public String getMapperType() {
        return "kh";
    }

    /**
     * GUID 추출, 링크에서 기사 ID를 추출하여 사용
     *
     * @param entry RSS 항목
     * @param source RSS 소스 정보
     * @return 신문사 코드 + 기사 ID 형태의 GUID
     * @throws ArticleCollectorException 링크가 없거나 ID 추출 실패 시
     * @since 2025-05-10
     */
    @Override
    protected String extractGuid(SyndEntry entry, RssSource source) {
        String uniqueId = extractUniqueIdFromLink(entry.getLink());
        return source.getCodePrefix() + uniqueId;
    }

    /**
     * 경향신문 링크에서 고유 ID 추출
     *
     * @param link 기사 링크
     * @return 추출된 고유 ID
     * @throws ArticleCollectorException 링크가 null이거나 ID를 추출할 수 없는 경우
     * @since 2025-05-10
     */
    private String extractUniqueIdFromLink(String link) {
        validateLink(link);

        try {
            String[] pathParts = splitLinkPath(link);
            return findArticleIdInPath(pathParts);
        } catch (Exception e) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.ITEM_MAPPING_ERROR, e);
        }
    }

    /**
     * 링크 유효성 검사
     *
     * @param link 검사할 링크
     * @throws ArticleCollectorException 링크가 null이거나 비어있는 경우
     * @since 2025-05-10
     */
    private void validateLink(String link) {
        if (link == null || link.trim().isEmpty()) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.ITEM_MAPPING_ERROR);
        }
    }

    /**
     * 링크를 경로 부분으로 분리
     *
     * @param link 분리할 링크
     * @return 경로 부분 배열
     * @since 2025-05-10
     */
    private String[] splitLinkPath(String link) {
        return link.split("/");
    }

    /**
     * 경로 부분에서 기사 ID 찾기
     *
     * @param pathParts 경로 부분 배열
     * @return 기사 ID
     * @throws ArticleCollectorException 기사 ID를 찾을 수 없는 경우
     * @since 2025-05-10
     */
    private String findArticleIdInPath(String[] pathParts) {
        for (int i = 0; i < pathParts.length; i++) {
            if ("article".equals(pathParts[i]) && i + 1 < pathParts.length) {
                String id = pathParts[i + 1];
                if (isValidArticleId(id)) {
                    return id;
                }
            }
        }

        throw new ArticleCollectorException(ArticleCollectorErrorCode.ITEM_MAPPING_ERROR);
    }

    /**
     * 기사 ID 유효성 검사
     *
     * @param id 검사할 ID
     * @return 유효성 여부
     * @since 2025-05-10
     */
    private boolean isValidArticleId(String id) {
        return id != null && !id.trim().isEmpty();
    }

    /**
     * 발행일 추출, 경향신문은 dc:date 태그 확인
     *
     * @param entry RSS 항목
     * @return 발행일 LocalDateTime
     * @since 2025-05-10
     */
    @Override
    protected LocalDateTime extractPubDate(SyndEntry entry) {
        if (entry.getPublishedDate() != null) {
            return convertToLocalDateTime(entry.getPublishedDate());
        }

        return extractDcDate(entry);
    }

    /**
     * date 태그에서 발행일 추출
     *
     * @param entry RSS 항목
     * @return 추출된 발행일, 없으면 현재 시간
     * @since 2025-05-10
     */
    private LocalDateTime extractDcDate(SyndEntry entry) {
        return entry.getForeignMarkup().stream()
                .filter(element -> "date".equals(element.getName()) &&
                        "dc".equals(element.getNamespacePrefix()))
                .findFirst()
                .map(element -> parseDateTime(element.getValue()))
                .orElse(LocalDateTime.now());
    }

    /**
     * 문자열을 LocalDateTime으로 파싱
     *
     * @param dateString 날짜 문자열
     * @return 파싱된 LocalDateTime, 실패 시 현재 시간
     * @since 2025-05-10
     */
    private LocalDateTime parseDateTime(String dateString) {
        try {
            return LocalDateTime.parse(dateString);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    /**
     * 카테고리 정보 추출
     *
     * @param entry RSS 항목
     * @param source RSS 소스 정보
     * @return 결합된 카테고리 문자열
     * @since 2025-05-10
     */
    @Override
    protected String extractCategory(SyndEntry entry, RssSource source) {
        return source.getCategoryName();
    }


}