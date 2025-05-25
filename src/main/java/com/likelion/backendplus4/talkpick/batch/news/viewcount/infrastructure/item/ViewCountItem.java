package com.likelion.backendplus4.talkpick.batch.news.viewcount.infrastructure.item;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 뉴스 조회수 데이터를 표현하는 모델 클래스입니다.
 * Spring Batch 처리 과정에서 Reader, Processor, Writer 간 데이터 전달을 위해 사용됩니다.
 *
 * @since 2025-05-20
 */
@Getter
@Setter
@NoArgsConstructor
public class ViewCountItem {
    private String newsId;
    private Long viewCount;

    /**
     * 뉴스 ID와 조회수로 객체를 생성합니다.
     *
     * @param newsId 뉴스 ID
     * @param viewCount 조회수
     */
    public ViewCountItem(String newsId, Long viewCount) {
        this.newsId = newsId;
        this.viewCount = viewCount;
    }
}