package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.repository;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface NewsInfoJpaRepository extends JpaRepository<ArticleEntity, Long> {

    boolean existsByLink(String link);

    /**
     * 특정 언론사의 가장 최신 기사 발행일 조회
     *
     * @param guidPrefix 언론사 GUID 접두어 (예: "KM", "DA", "KH")
     * @return 가장 최신 발행일
     */
    @Query("SELECT MAX(a.pubDate) FROM ArticleEntity a WHERE a.guid LIKE CONCAT(:guidPrefix, '%')")
    LocalDateTime findLatestPubDateByGuidPrefix(@Param("guidPrefix") String guidPrefix);
    
    /**
     * summary가 null인 ArticleEntity 목록을 페이지 형태로 조회한다.
     *
     * @param pageable 페이징 정보
     * @return summary가 null인 ArticleEntity 페이지
     * @author 함예정
     * @date 2025-05-17
     */
    Page<ArticleEntity> findBySummaryIsNull(Pageable pageable);

    /**
     * summary는 존재하지만 summaryVector는 null인 ArticleEntity 목록을 페이지 형태로 조회한다.
     *
     * @param pageable 페이징 정보
     * @return summary는 존재하고 summaryVector는 null인 ArticleEntity 페이지
     * @author 함예정
     * @date 2025-05-17
     */
    Page<ArticleEntity> findBySummaryIsNotNullAndSummaryVectorIsNull(Pageable pageable);
}