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
     * 요약 정보가 없는 뉴스 개수 조회
     * @return 요약 정보 없는 뉴스 개수
     * @author 함예정
     * @since 2025-05-17
     */
    long countBySummaryIsNull();

    Page<ArticleEntity> findBySummaryIsNull(Pageable pageable);
}