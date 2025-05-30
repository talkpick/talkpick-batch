package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.quartz.job;

import java.util.List;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.domain.model.NewsInfo;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.elasticsearch.ElasticsearchNewsInfoAdapter;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.jpa.entity.ArticleEntity;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.jpa.mapper.ArticleEntityMapper;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.jpa.repository.NewsInfoJpaRepository;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.quartz.support.JobDataManager;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.adapter.quartz.support.JobDataManagerBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 뉴스 기사 정보를 Elasticsearch에 인덱싱하는 Quartz Job입니다. <p>
 * 마지막 인덱싱된 기사 ID를 기준으로 이후의 기사만 처리합니다. <p>
 * 마지막 인덱싱 기사 ID는 Quartz의 JobDataMap에 저장하여 상태를 유지합니다. <p>
 *
 * PersistJobDataAfterExecution - Job 실행이 끝난 후에도 JobDataMap의 데이터를 유지하여 다음 실행에 활용 가능하게 함 <p>
 * DisallowConcurrentExecution - 하나의 Job 인스턴스가 아직 실행 중일 때 같은 Job 클래스의 중복 실행을 방지 <p>
 *
 * @since 2025-05-29
 */
@Slf4j
@Component
@RequiredArgsConstructor
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class IndexQuartzJob implements Job {
	private static final String LAST_INDEX_ITEM_ID = "lastIndexItemId";
	private final NewsInfoJpaRepository newsInfoJpaRepository;
	private final ElasticsearchNewsInfoAdapter elasticsearchAdapter;

	/**
	 * Quartz Job의 실행 로직을 구현합니다. <p>
	 * 마지막 인덱스 이후의 기사들을 Elasticsearch에 저장하며, <p>
	 * 마지막으로 처리된 기사 ID를 JobDataMap에 저장하여 다음 실행 시 활용합니다. <p>
	 *
	 * @param jobExecutionContext Job 실행 컨텍스트
	 * @author 함예정
	 * @since 2025-05-29
	 */
	@Override
	public void execute(JobExecutionContext jobExecutionContext) {
		JobDataManager jobData = JobDataManagerBuilder.buildJobManager(jobExecutionContext);
		long lastIndex = getLastIndexItemId(jobData);

		List<ArticleEntity> articleEntities = getArticleEntities(lastIndex);

		if (isEmptyArticleEntity(articleEntities)) {
			return;
		}

		List<NewsInfo> newsInfos = toDomain(articleEntities);
		elasticsearchAdapter.saveAll(newsInfos);

		jobData.saveLong(LAST_INDEX_ITEM_ID, articleEntities.getLast().getId());

	}

	/**
	 * 마지막으로 인덱싱된 뉴스 기사 ID를 조회합니다. <p>
	 * JobDataMap에 해당 키가 존재하지 않으면, <p>
	 * summaryVector 값이 존재하는 뉴스 데이터 중 가장 작은 ID를 조회하여 시작점으로 사용합니다. <p>
	 * 이는 summaryVector가 1536차원이 아닌 경우 인덱싱 오류가 발생할 수 있기 때문에, <br>
	 * 유효한 summaryVector 값을 가진 기사 중 가장 낮은 ID를 안전한 시작점으로 삼습니다.
	 *
	 * @param dataManager JobDataMap 접근 유틸리티
	 * @return 마지막 인덱싱된 ID 또는 시작 ID
	 * @author 함예정
	 * @since 2025-05-29
	 */
	private long getLastIndexItemId(JobDataManager dataManager) {
		return dataManager.getLong(
			LAST_INDEX_ITEM_ID,
			newsInfoJpaRepository::findMinIdBySummaryVectorIsNotNull
		);
	}

	/**
	 * 주어진 ID 이후의 요약 벡터가 존재하는 기사 목록을 조회합니다.
	 *
	 * @param lastIndex 마지막 인덱싱 ID
	 * @return 조회된 기사 리스트
	 * @author 함예정
	 * @since 2025-05-29
	 */
	private List<ArticleEntity> getArticleEntities(long lastIndex) {
		return newsInfoJpaRepository.findAllBySummaryVectorIsNotNullAndIdGreaterThanOrderById(lastIndex);
	}

	/**
	 * 기사 리스트가 비어있는지 확인합니다.
	 *
	 * @param articleEntities 기사 리스트
	 * @return 비어 있으면 true, 아니면 false
	 * @author 함예정
	 * @since 2025-05-29
	 */
	private boolean isEmptyArticleEntity(List<ArticleEntity> articleEntities) {
		return articleEntities == null || articleEntities.isEmpty();
	}

	/**
	 * ArticleEntity 리스트를 도메인 모델인 NewsInfo 리스트로 변환합니다.
	 *
	 * @param articleEntities JPA 엔티티 리스트
	 * @return 변환된 도메인 리스트
	 * @author 함예정
	 * @since 2025-05-29
	 *
	 * TODO: 도메인 객체 변환은 인프라에서 담당하도록 수정 필요
	 */
	private List<NewsInfo> toDomain(List<ArticleEntity> articleEntities) {
		return articleEntities.stream()
			.map(ArticleEntityMapper::toDomainFromEntity)
			.toList();
	}
}
