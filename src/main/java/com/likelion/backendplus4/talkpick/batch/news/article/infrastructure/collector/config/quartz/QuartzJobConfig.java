package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.collector.config.quartz;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RSS 피드를 정기적으로 수집하는 스케줄 작업
 * application.yml의 rss.scheduler.cron 속성으로 실행 주기 설정
 * 설정이 없을 경우 기본값으로 1분마다 실행
 *
 * 1. 서비스를 거쳐서 피드 수집
 * 2. 처리된 항목 수 로깅
 * 3. 예외 발생 시 오류 로깅하고 다음 스케쥴까지 대기
 *
 * @modified 2025-05-11 Spring Scheduler 에서 Quartz Scheduler 으로 전환
 * @modified 2025-05-10 cron 표현식을 application 으로 분리
 * @since 2025-05-10 최초 작성
 *
 */
@Configuration
public class QuartzJobConfig {
	private final String articleCollectorJobDetailName = "articleCollectorJobDetail";


	/**
	 * RSS 수집 Quartz JobDetail 빈 등록.
	 * Job 클래스는 {@link BatchJobExecutor}이며 다음과 같은 설정을 포함한다:
	 * - withIdentity("rssBatchJob"): Scheduler 내에서 이 Job을 고유하게 식별하기 위한 이름 지정
	 * - storeDurably(): Trigger가 없더라도 Scheduler에 등록된 상태로 유지되도록 설정
	 *
	 * @return RSS 배치 작업용 JobDetail 객체
	 * @author 함예정
	 * @since 2025-05-10
	 */
	@Bean
	public JobDetail articleCollectorJobDetail() {
		return JobBuilder.newJob(BatchJobExecutor.class)
			.withIdentity(articleCollectorJobDetailName)
			.storeDurably()
			.build();
	}

}
