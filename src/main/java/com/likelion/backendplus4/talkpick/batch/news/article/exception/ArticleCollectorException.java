package com.likelion.backendplus4.talkpick.batch.news.article.exception;

import com.likelion.backendplus4.talkpick.batch.common.exception.CustomException;
import com.likelion.backendplus4.talkpick.batch.common.exception.error.ErrorCode;

/**
 * RSS 예외처리 클래스
 *
 * @since 2025-05-10
 * @modified 2025-05-11
 *   - 클래스에서 저자 삭제 (메소드에 저자 추가)
 *   - 코드 컨벤션에 맞도록 CustomException 상속으로 변경 (변경 전: RuntimeException 상속)
 */
public class ArticleCollectorException extends CustomException {
    private final ErrorCode errorCode;

    public ArticleCollectorException(ErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }

    /**
     *
     * @return
     */
    @Override
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}