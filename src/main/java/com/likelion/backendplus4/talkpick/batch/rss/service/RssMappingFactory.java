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
        mappers.put("km", kmibRssMapper);
    }

    /**
     * 소스 타입에 맞는 매퍼 반환
     *
     * @param type 매퍼 타입 (소문자 언론사 코드)
     * @return 해당 타입의 RSS 매퍼
     * @throws IllegalArgumentException 지원하지 않는 타입인 경우
     */
    public RssMapper getMapper(String type) {
        RssMapper mapper = mappers.get(type);
        if (mapper == null) {
            throw new IllegalArgumentException("Mapper 없음: " + type);
        }
        return mapper;
    }
}