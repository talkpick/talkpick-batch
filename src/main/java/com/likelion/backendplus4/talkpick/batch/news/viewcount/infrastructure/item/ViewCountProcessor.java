package com.likelion.backendplus4.talkpick.batch.news.viewcount.infrastructure.item;

import org.springframework.batch.item.ItemProcessor;

/**
 * 조회수 데이터를 처리하는 ItemProcessor 구현 클래스입니다.
 * 현재는 데이터를 그대로 전달하지만, 필요한 경우 추가 처리 로직을 구현할 수 있습니다.
 *
 * @since 2025-05-20
 */
public class ViewCountProcessor implements ItemProcessor<ViewCountItem, ViewCountItem> {

    /**
     * 입력 항목을 처리하고 결과를 반환합니다.
     * 현재는 단순히 항목을 통과시킵니다.
     *
     * @param item 처리할 조회수 항목
     * @return 처리된 조회수 항목
     */
    @Override
    public ViewCountItem process(ViewCountItem item) {
        return item;
    }
}