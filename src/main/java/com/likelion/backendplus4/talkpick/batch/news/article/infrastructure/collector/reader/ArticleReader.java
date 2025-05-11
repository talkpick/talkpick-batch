package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.reader;

import java.util.Iterator;
import java.util.List;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.batch.RssSource;

import lombok.extern.slf4j.Slf4j;

/**
 * StepExecutionContext로부터 전달받은 RSS 소스 리스트를 순차적으로 제공하는 ItemReader 구현체.
 * Spring Batch의 Step 내부에서 Partition 단위로 각 소스를 하나씩 읽어 처리하는 데 사용된다.
 *
 * @since 2025-05-10
 */
@Slf4j
@Component
@StepScope
public class ArticleReader implements ItemReader<RssSource> {

	private final Iterator<RssSource> iterator;

	/**
	 * StepExecutionContext에 저장된 RSS 소스 리스트를 기반으로 Iterator를 초기화한다.
	 *
	 * @param sources StepExecutionContext로부터 전달된 RSS 소스 리스트
	 * @since 2025-05-10
	 * @author 함예정
	 */
	public ArticleReader(@Value("#{stepExecutionContext['sourceList']}") List<RssSource> sources) {
		this.iterator = sources.iterator();
	}

	/**
	 * RSS 소스를 하나씩 반환하며, 더 이상 남은 소스가 없으면 null을 반환한다.
	 * null 반환 시 해당 Step의 실행이 종료된다.
	 *
	 * @return 하나의 RssSource 또는 null
	 * @since 2025-05-10
	 * @author 함예정
	 */
	@Override
	public RssSource read() {
		return iterator.hasNext() ? iterator.next() : null;
	}
}
