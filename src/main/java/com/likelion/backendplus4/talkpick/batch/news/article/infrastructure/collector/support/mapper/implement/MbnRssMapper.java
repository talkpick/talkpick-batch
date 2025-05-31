//package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.implement;
//
//import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
//import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;
//import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.batch.RssSource;
//import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.AbstractRssMapper;
//import com.rometools.rome.feed.synd.SyndEntry;
//
//import org.springframework.stereotype.Component;
//
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
///**
// * MBN RSS 매퍼 구현체
// *
// * @author 양병학
// * @since 2025-05-12 최초 작성
// */
//@Component
//public class MbnRssMapper extends AbstractRssMapper {
//
//    private static final Pattern NEWS_ID_PATTERN = Pattern.compile("news_seq_no=([0-9]+)");
//
//    /**
//     * Mapper의 타입 반환
//     *
//     * @return Mapper 타입 (mb)
//     */
//    @Override
//    public String getMapperType() {
//        return "mb";
//    }
//
//    /**
//     * GUID 추출, 링크에서 뉴스 ID를 추출하여 생성
//     *
//     * @param entry RSS 항목
//     * @param source RSS 소스 정보
//     * @return 형식: [언론사코드][뉴스ID]
//     */
//    @Override
//    protected String extractGuid(SyndEntry entry, RssSource source) {
//        String newsId = extractNewsIdFromLink(entry.getLink());
//        return source.getCodePrefix() + newsId;
//    }
//
//    /**
//     * 링크에서 뉴스 ID 값 추출
//     *
//     * @param link 기사 링크
//     * @return 추출된 뉴스 ID, 없으면 타임스탬프 반환
//     */
//    private String extractNewsIdFromLink(String link) {
//        if (link == null) {
//            return String.valueOf(System.currentTimeMillis());
//        }
//
//        try {
//            Matcher matcher = NEWS_ID_PATTERN.matcher(link);
//            if (matcher.find()) {
//                return matcher.group(1);
//            }
//        } catch (Exception e) {
//            throw new ArticleCollectorException(ArticleCollectorErrorCode.ITEM_MAPPING_ERROR);
//        }
//
//        return String.valueOf(System.currentTimeMillis());
//    }
//
//    /**
//     * 카테고리 추출 메서드
//     *
//     * @param entry RSS 항목
//     * @param source RSS 소스 정보
//     * @return 카테고리
//     */
//    @Override
//    protected String extractCategory(SyndEntry entry, RssSource source) {
//        return source.getCategoryName();
//    }
//}