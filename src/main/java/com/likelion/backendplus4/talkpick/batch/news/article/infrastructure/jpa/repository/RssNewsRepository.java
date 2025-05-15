package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.repository;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RssNewsRepository extends JpaRepository<ArticleEntity, Long> {

    boolean existsByLink(String link);
}