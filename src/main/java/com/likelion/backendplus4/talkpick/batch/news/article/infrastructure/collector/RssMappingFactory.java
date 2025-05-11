package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.RssMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RssMappingFactory {

    private final Map<String, RssMapper> mappers = new HashMap<>();

    /**
     * 모든 RssMapper 구현체를 자동으로 주입받아 맵에 등록
     *
     * @param availableMappers RssMapper 구현체 목록
     */
    @Autowired
    public RssMappingFactory(List<RssMapper> availableMappers) {
        for (RssMapper mapper : availableMappers) {
            String mapperType = mapper.getMapperType();
            mappers.put(mapperType, mapper);
        }
    }

    /**
     * 타입에 맞게 mapper 반환
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