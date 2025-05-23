package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;

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
}