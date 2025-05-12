package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.support.mapper.AbstractRssMapper;

/**
 * RSS 매핑 전략을 관리하는 팩토리 클래스
 *
 * @since 2025-05-10
 * @modified 2025-05-13 RssMapper 인터페이스 대신 AbstractRssMapper 사용
 */
@Component
public class RssMappingFactory {

    private final Map<String, AbstractRssMapper> mappers = new HashMap<>();

    /**
     * AbstractRssMapper 구현체를 받아서 Mapper에 등록
     *
     * @param availableMappers AbstractRssMapper List 목록
     */
    @Autowired
    public RssMappingFactory(List<AbstractRssMapper> availableMappers) {
        for (AbstractRssMapper mapper : availableMappers) {
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
    public AbstractRssMapper getMapper(String type) {
        AbstractRssMapper mapper = mappers.get(type);
        if (mapper == null) {
            throw new IllegalArgumentException("Mapper 없음: " + type);
        }
        return mapper;
    }
}