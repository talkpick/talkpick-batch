package com.likelion.backendplus4.talkpick.batch.rss.repository;

import com.likelion.backendplus4.talkpick.batch.rss.entity.RssNews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RssNewsRepository extends JpaRepository<RssNews, Long> {

    boolean existsByLink(String link);
}