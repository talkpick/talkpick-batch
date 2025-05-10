package com.likelion.backendplus4.talkpick.batch.rss.service;

import com.likelion.backendplus4.talkpick.batch.rss.service.mapper.KmibRssMapper;
import com.likelion.backendplus4.talkpick.batch.rss.service.mapper.RssMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RssMappingFactory {

    private final Map<String, RssMapper> mappers = new HashMap<>();

    public RssMappingFactory(KmibRssMapper kmibRssMapper) {
        // mappers.put("donga", dongaRssMapper);
        // mappers.put("khan", khanRssMapper);
        mappers.put("kmib", kmibRssMapper);
    }

    public RssMapper getMapper(String type) {
        RssMapper mapper = mappers.get(type);
        if (mapper == null) {
            throw new IllegalArgumentException("Mapper 없음: " + type);
        }
        return mapper;
    }
}