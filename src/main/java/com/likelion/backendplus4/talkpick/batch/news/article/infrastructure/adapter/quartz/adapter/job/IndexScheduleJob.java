package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.quartz.adapter.job;

import java.util.List;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.domain.model.NewsInfo;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.elasticsearch.ElasticsearchNewsInfoAdapter;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.jpa.entity.ArticleEntity;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.jpa.mapper.ArticleEntityMapper;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.jpa.repository.NewsInfoJpaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class IndexScheduleJob implements Job {
	private static final String LAST_INDEX_ITEM_ID = "lastIndexItemId";
	private final NewsInfoJpaRepository newsInfoJpaRepository;
	private final ElasticsearchNewsInfoAdapter elasticsearchAdapter;

	@Override
	public void execute(JobExecutionContext jobExecutionContext) {
		try {
			log.info("IndexScheduleJob 시작");
			if(jobExecutionContext != null) {
				JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
				long lastIndex = getLastIndexItemId(jobDataMap);
				log.info("|- lastIndex = {}", lastIndex);
			}

			List<ArticleEntity> articleEntities = getArticleEntities(1103);

			List<NewsInfo> newsInfos = articleEntities.stream()
				.map(ArticleEntityMapper::toDomainFromEntity)
				.toList();
			elasticsearchAdapter.saveAll(newsInfos);

			// saveLastIndex(articleEntities.getLast().getId(), jobDataMap);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void saveLastIndex(long lastId, JobDataMap jobDataMap){
		jobDataMap.put(LAST_INDEX_ITEM_ID, lastId);
		log.info("|- savelastId = {}", lastId);

	}
	private List<ArticleEntity> getArticleEntities(long lastIndex) {
		return newsInfoJpaRepository.findAllByIdGreaterThanOrderById(lastIndex);
	}

	private long getLastIndexItemId(JobDataMap jobDataMap) {
		return jobDataMap.containsKey(LAST_INDEX_ITEM_ID) ?
			jobDataMap.getLong("lastIndexItemId") :
			newsInfoJpaRepository.findMinIdBySummaryIsNotNull();
	}


}
