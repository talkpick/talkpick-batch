package com.likelion.backendplus4.talkpick.batch.news.viewcount.infrastructure.item;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.batch.item.ItemReader;
import org.springframework.data.redis.core.RedisTemplate;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.ViewCountInvalidFormatException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.viewcount.infrastructure.batch.config.NewsViewCountProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * Redis에서 뉴스 조회수 데이터를 읽는 ItemReader 구현 클래스입니다.
 * 특정 패턴에 일치하는 모든 Redis 키를 대상으로 조회수 데이터를 가져옵니다.
 *
 * @since 2025-05-20
 * @modified 2025-05-24 코드 가독성 향상을 위한 리팩토링
 */
@Slf4j
public class ViewCountRedisReader implements ItemReader<ViewCountItem> {

    private static final String VIEW_COUNT_KEY_PREFIX = "news:viewCount:";

    private final RedisTemplate<String, String> redisTemplate;
    private final String keyPattern;
    private Iterator<String> keyIterator;
    private final Pattern validNewsIdPattern;
    private final Set<String> validPrefixes;

    /**
     * Redis 템플릿과 키 패턴을 받아 Reader를 초기화합니다.
     *
     * @param redisTemplate Redis 작업을 위한 템플릿
     * @param keyPattern 가져올 키 패턴 (예: "news:viewCount:*")
     * @param properties 뉴스 조회수 설정 프로퍼티
     */
    public ViewCountRedisReader(
            RedisTemplate<String, String> redisTemplate,
            String keyPattern,
            NewsViewCountProperties properties) {
        this.redisTemplate = redisTemplate;
        this.keyPattern = keyPattern;

        // 유효한 접두사 설정
        this.validPrefixes = new HashSet<>(Arrays.asList(properties.getValidPrefixes().split(",")));

        // 유효한 접두사를 사용한 정규식 패턴 생성
        StringBuilder patternBuilder = new StringBuilder("^(");
        patternBuilder.append(String.join("|", validPrefixes));
        patternBuilder.append(")\\d+$");

        this.validNewsIdPattern = Pattern.compile(patternBuilder.toString());

        log.info("뉴스 ID 유효성 검사 패턴 초기화: {}", patternBuilder);
        log.info("유효한 뉴스 ID 접두사: {}", validPrefixes);
    }

    /**
     * Redis에서 다음 조회수 항목을 읽어옵니다.
     * 모든 항목을 처리한 경우 null을 반환합니다.
     *
     * 1. 키 목록 초기화(필요한 경우)
     * 2. 다음 키 조회
     * 3. 키에서 조회수 항목 변환
     *
     * @return 조회수 항목 또는 null (처리 완료 시)
     * @since 2025-05-20 최초 작성
     * @author 양병학
     *
     */
    @Override
    public ViewCountItem read() {
        initializeKeysIfNeeded();

        if (keyIterator == null || !keyIterator.hasNext()) {
            return null;
        }

        String key = keyIterator.next();
        return getViewCountItemFromKey(key);
    }

    /**
     * 필요한 경우 Redis에서 키 목록을 초기화합니다.
     */
    private void initializeKeysIfNeeded() {
        if (keyIterator == null) {
            try {
                Set<String> keys = redisTemplate.keys(keyPattern);
                log.debug("Redis에서 {}개의 조회수 키를 가져왔습니다.", keys != null ? keys.size() : 0);

                if (keys != null && !keys.isEmpty()) {
                    keyIterator = keys.iterator();
                }
            } catch (Exception e) {
                log.error("Redis 키 초기화 중 오류 발생: {}", e.getMessage());
                throw new ArticleCollectorException(ArticleCollectorErrorCode.VIEW_COUNT_SYNC_FAILED, e);
            }
        }
    }

    /**
     * Redis 키로부터 ViewCountItem을 조회합니다.
     *
     * @param key 처리할 Redis 키
     * @return 변환된 ViewCountItem 또는 null (값이 없는 경우)
     */
    private ViewCountItem getViewCountItemFromKey(String key) {
        try {
            String newsId = extractNewsIdFromKey(key);

            // 유효한 뉴스 ID 패턴인지 확인
            if (!isValidNewsId(newsId)) {
                log.warn("유효하지 않은 뉴스 ID 형식이 감지되었습니다. 이 항목은 처리되지 않습니다: {}", newsId);
                return null;
            }

            String countValue = fetchValueFromRedis(key);

            if (countValue == null) {
                log.debug("키 {}에 대한 값이 없습니다.", key);
                return null;
            }

            Long viewCount = parseViewCount(countValue, key);
            return new ViewCountItem(newsId, viewCount);
        } catch (ViewCountInvalidFormatException e) {
            throw e;
        } catch (Exception e) {
            log.error("Redis 값 처리 중 오류 발생: {}", e.getMessage());
            throw new ArticleCollectorException(ArticleCollectorErrorCode.VIEW_COUNT_SYNC_FAILED, e);
        }
    }

    /**
     * 뉴스 ID가 유효한 패턴인지 확인합니다.
     * 설정된 접두사로 시작하는 숫자 형식만 유효합니다.
     *
     * @param newsId 확인할 뉴스 ID
     * @return 유효하면 true, 아니면 false
     */
    private boolean isValidNewsId(String newsId) {
        // 정규식 패턴을 사용한 검증
        return validNewsIdPattern.matcher(newsId).matches();
    }

    /**
     * Redis 키에서 뉴스 ID를 추출합니다.
     *
     * @param key Redis 키
     * @return 추출된 뉴스 ID
     */
    private String extractNewsIdFromKey(String key) {
        return key.substring(VIEW_COUNT_KEY_PREFIX.length());
    }

    /**
     * Redis에서 키에 해당하는 값을 가져옵니다.
     *
     * @param key Redis 키
     * @return 키에 해당하는 값 또는 null
     */
    private String fetchValueFromRedis(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 문자열 값을 조회수(Long)로 파싱합니다.
     *
     * @param countValue 파싱할 문자열 값
     * @param key 관련 Redis 키 (로깅 및 오류 메시지용)
     * @return 파싱된 조회수 값
     * @throws ViewCountInvalidFormatException 파싱 실패 시
     */
    private Long parseViewCount(String countValue, String key) {
        try {
            return Long.parseLong(countValue);
        } catch (NumberFormatException e) {
            log.warn("잘못된 조회수 형식: key={}, value={}", key, countValue);
            throw new ViewCountInvalidFormatException(
                    "Redis 키 '" + key + "'의 값이 유효한 숫자 형식이 아닙니다: " + countValue, e);
        }
    }
}