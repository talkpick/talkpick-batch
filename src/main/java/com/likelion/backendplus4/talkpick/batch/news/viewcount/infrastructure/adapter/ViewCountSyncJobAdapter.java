package com.likelion.backendplus4.talkpick.batch.news.viewcount.infrastructure.adapter;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.news.article.exception.ArticleCollectorException;
import com.likelion.backendplus4.talkpick.batch.news.article.exception.error.ArticleCollectorErrorCode;
import com.likelion.backendplus4.talkpick.batch.news.viewcount.application.port.out.ViewCountSyncJobPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ViewCountSyncJobAdapter implements ViewCountSyncJobPort {

    private final JobLauncher jobLauncher;
    private final Job viewCountSyncJob;

    @Override
    public void executeJob(String requestor) {
        try {
            JobParameters params = createJobParameters(requestor);
            jobLauncher.run(viewCountSyncJob, params);
        } catch (Exception e) {
            throw new ArticleCollectorException(ArticleCollectorErrorCode.VIEW_COUNT_SYNC_FAILED, e);
        }
    }

    private JobParameters createJobParameters(String requestor) {
        return new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addString("requestedBy", requestor)
                .toJobParameters();
    }
}