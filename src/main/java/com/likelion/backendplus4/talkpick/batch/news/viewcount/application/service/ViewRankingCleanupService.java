package com.likelion.backendplus4.talkpick.batch.news.viewcount.application.service;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.batch.vo.NewsCategory;
import com.likelion.backendplus4.talkpick.batch.news.viewcount.application.port.in.ViewRankingCleanupUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Redis 정리 작업을 담당하는 서비스 클래스입니다.
 *
 * 3일 이상 지난 뉴스 랭킹 데이터를 정리하여 메모리 사용량을 최적화합니다.
 *

 * @since 2025-05-29 최초 작성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ViewRankingCleanupService implements ViewRankingCleanupUseCase {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int CLEANUP_DAYS = 3;

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 3일 이상 지난 뉴스 랭킹 키들을 정리합니다.
     *
     * 1. news:ranking:* 패턴으로 모든 랭킹 키 조회
     * 2. 3일 이전 날짜를 가진 키들 필터링
     * 3. 배치 삭제 실행
     * 4. 정리 결과 로깅
     *
     * @author 양병학
     * @since 2025-05-29 최초 작성
     */
    @Override
    public void cleanupOldRankingKeys() {
        log.info("{}일 이상 된 뉴스 랭킹 데이터 정리 작업을 시작합니다", CLEANUP_DAYS);

        Set<String> rankingKeys = findAllRankingKeys();
        long cutoffEpochDay = calculateCutoffEpochDay();

        int totalDeleted = processRankingCleanup(rankingKeys, cutoffEpochDay);

        log.info("뉴스 랭킹 데이터 정리 작업이 완료되었습니다. 총 {}개 항목을 삭제했습니다", totalDeleted);
    }

    private Set<String> findAllRankingKeys() {
        Set<String> rankingKeys = redisTemplate.keys("news:ranking:*");
        if (rankingKeys == null || rankingKeys.isEmpty()) {
            log.info("정리할 랭킹 키를 찾을 수 없습니다");
            return Set.of();
        }
        return rankingKeys;
    }

    private long calculateCutoffEpochDay() {
        return LocalDate.now().minusDays(CLEANUP_DAYS).toEpochDay();
    }

    private int processRankingCleanup(Set<String> rankingKeys, long cutoffEpochDay) {
        return rankingKeys.stream()
                .mapToInt(key -> cleanupSingleRanking(key, cutoffEpochDay))
                .sum();
    }

    private int cleanupSingleRanking(String rankingKey, long cutoffEpochDay) {
        List<String> expiredEntries = findExpiredEntries(rankingKey, cutoffEpochDay);
        return deleteExpiredEntries(rankingKey, expiredEntries);
    }

    private List<String> findExpiredEntries(String rankingKey, long cutoffEpochDay) {
        Set<ZSetOperations.TypedTuple<String>> allData =
                redisTemplate.opsForZSet().rangeWithScores(rankingKey, 0, -1);

        return allData.stream()
                .filter(tuple -> isExpiredEntry(tuple.getScore(), cutoffEpochDay))
                .map(ZSetOperations.TypedTuple::getValue)
                .collect(Collectors.toList());
    }

    private boolean isExpiredEntry(Double score, long cutoffEpochDay) {
        long epochDay = (long)(score % 100000);
        return epochDay < cutoffEpochDay;
    }

    private int deleteExpiredEntries(String rankingKey, List<String> expiredEntries) {
        if (expiredEntries.isEmpty()) {
            return 0;
        }

        Long deletedCount = redisTemplate.opsForZSet().remove(rankingKey, expiredEntries.toArray());
        log.debug("{}에서 {}개 항목을 삭제했습니다", rankingKey, deletedCount);
        return expiredEntries.size();
    }
}