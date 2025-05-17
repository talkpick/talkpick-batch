package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.summary.batch.reader;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity.ArticleEntity;

import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@StepScope
public class ArticleSummaryPageReader extends JpaPagingItemReader<ArticleEntity> {

	public ArticleSummaryPageReader(
		EntityManagerFactory entityManagerFactory,
		@Value("#{stepExecutionContext[startPage]}") int startPage,
		@Value("#{stepExecutionContext[endPage]}") int endPage
	) {
		int pageSize = 100;
		int startItem = (startPage - 1) * pageSize;
		int endItem = endPage * pageSize;
		logger.info(pageSize + " " + startItem + " " + endItem);

		this.setEntityManagerFactory(entityManagerFactory);
		this.setQueryString("SELECT n FROM ArticleEntity n WHERE n.summary IS NULL");
		this.setPageSize(pageSize);
		this.setCurrentItemCount(startItem);
		this.setMaxItemCount(endItem);
		this.setSaveState(false);
		this.setName("articleReader-" + startPage + "-" + endPage);
	}
}
