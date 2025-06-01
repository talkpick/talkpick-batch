package com.likelion.backendplus4.talkpick.batch.news.viewcount.application.service;

import com.likelion.backendplus4.talkpick.batch.news.viewcount.application.port.in.ViewRankingCleanupUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Redis 정리 작업을 담당하는 서비스 클래스입니다.
 * <p>
 * 3일 이상 지난 뉴스 랭킹 데이터를 정리하여 메모리 사용량을 최적화합니다.
 *
 * @since 2025-05-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ViewRankingCleanupService implements ViewRankingCleanupUseCase {

    private static final int CLEANUP_DAYS = 3;

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 3일 이상 지난 뉴스 랭킹 키들을 정리합니다.
     * <p>
     * 1. news:ranking:* 패턴으로 모든 랭킹 키 조회
     * 2. 3일 이전 날짜를 가진 키들 필터링
     * 3. 배치 삭제 실행
     * 4. 정리 결과 로깅
     *
     * @author 양병학
     * @since 2025-05-29
     */
    @Override
    public void cleanupOldRankingKeys() {
        log.info("{}일 이상 된 뉴스 랭킹 데이터 정리 작업을 시작합니다", CLEANUP_DAYS);

        Set<String> rankingKeys = findAllRankingKeys();
        long cutoffEpochDay = calculateCutoffEpochDay();

        int totalDeleted = processRankingCleanup(rankingKeys, cutoffEpochDay);

        log.info("뉴스 랭킹 데이터 정리 작업이 완료되었습니다. 총 {}개 항목을 삭제했습니다", totalDeleted);
    }

    /**
     * Redis에서 모든 랭킹 키를 조회합니다.
     *
     * @return 조회된 랭킹 키 집합
     */
    private Set<String> findAllRankingKeys() {
        Set<String> rankingKeys = redisTemplate.keys("news:ranking:*");
        if (rankingKeys == null || rankingKeys.isEmpty()) {
            log.info("정리할 랭킹 키를 찾을 수 없습니다");
            return Set.of();
        }
        return rankingKeys;
    }

    /**
     * 정리 기준 날짜를 계산합니다.
     * 현재 날짜에서 CLEANUP_DAYS만큼 이전 날짜의 epochDay(3일전 날짜)를 반환합니다.
     *
     * @return 기준 날짜 (epochDay 형태)
     */
    private long calculateCutoffEpochDay() {
        return LocalDate.now().minusDays(CLEANUP_DAYS).toEpochDay();
    }

    /**
     * 모든 랭킹 키에 대해 정리 작업을 수행합니다.
     * 각 키별로 만료된 항목을 찾아 삭제하고 총 삭제 개수를 반환합니다.
     *
     * @param rankingKeys    정리할 랭킹 키 집합
     * @param cutoffEpochDay 정리 기준 날짜
     * @return 삭제된 총 항목 수
     */
    private int processRankingCleanup(Set<String> rankingKeys, long cutoffEpochDay) {
        return rankingKeys.stream()
                .mapToInt(key -> cleanupSingleRanking(key, cutoffEpochDay))
                .sum();
    }

    /**
     * 단일 랭킹 키에서 만료된 항목들을 정리합니다.
     *
     * @param rankingKey     정리할 랭킹 키
     * @param cutoffEpochDay 정리 기준 날짜
     * @return 삭제된 항목 수
     */
    private int cleanupSingleRanking(String rankingKey, long cutoffEpochDay) {
        List<String> expiredEntries = findExpiredEntries(rankingKey, cutoffEpochDay);
        return deleteExpiredEntries(rankingKey, expiredEntries);
    }

    /**
     * 지정된 랭킹 키에서 만료된 항목들을 찾습니다.
     * 스코어의 날짜 부분이 기준 날짜보다 이전인 항목들을 필터링합니다.
     *
     * @param rankingKey     조회할 랭킹 키
     * @param cutoffEpochDay 정리 기준 날짜
     * @return 만료된 항목들의 값 목록
     */
    private List<String> findExpiredEntries(String rankingKey, long cutoffEpochDay) {
        Set<ZSetOperations.TypedTuple<String>> allData =
                redisTemplate.opsForZSet().rangeWithScores(rankingKey, 0, -1);

        return allData.stream()
                .filter(tuple -> isExpiredEntry(tuple.getScore(), cutoffEpochDay))
                .map(ZSetOperations.TypedTuple::getValue)
                .collect(Collectors.toList());
    }

    /**
     * 스코어를 기반으로 항목이 만료되었는지 확인합니다.
     * 스코어 형식: 조회수 × 10000 + 날짜(epochDay % 100000)
     *
     * @param score          확인할 스코어 값
     * @param cutoffEpochDay 정리 기준 날짜
     * @return 만료 여부 (true: 만료됨, false: 유효함)
     */
    private boolean isExpiredEntry(Double score, long cutoffEpochDay) {
        long epochDay = (long) (score % 100000);
        return epochDay < cutoffEpochDay;
    }

    /**
     * 지정된 랭킹 키에서 만료된 항목들을 삭제합니다.
     *
     * @param rankingKey     삭제할 랭킹 키
     * @param expiredEntries 삭제할 항목 목록
     * @return 실제 삭제된 항목 수
     */
    private int deleteExpiredEntries(String rankingKey, List<String> expiredEntries) {
        if (expiredEntries.isEmpty()) {
            return 0;
        }

        Long deletedCount = redisTemplate.opsForZSet().remove(rankingKey, expiredEntries.toArray());
        log.debug("{}에서 {}개 항목을 삭제했습니다", rankingKey, deletedCount);
        return expiredEntries.size();
    }
}