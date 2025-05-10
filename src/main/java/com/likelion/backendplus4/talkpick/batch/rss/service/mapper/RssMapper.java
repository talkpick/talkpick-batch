package com.likelion.backendplus4.talkpick.batch.rss.service.mapper;

import com.rometools.rome.feed.synd.SyndEntry;
import com.likelion.backendplus4.talkpick.batch.rss.entity.RssNews;

public interface RssMapper {
    RssNews mapToRssNews(SyndEntry entry);
}