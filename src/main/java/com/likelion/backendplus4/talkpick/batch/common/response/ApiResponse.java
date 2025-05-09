package com.likelion.backendplus4.talkpick.batch.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * API 응답 포맷 클래스 정상 및 에러 응답을 통합된 형식으로 제공한다.
 *
 * @since 2025-04-16
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ApiResponse<T> {

    private static final String SUCCESS_MESSAGE = "요청 성공";

    private String errorCode;
    private String message;
    private T data;

    public static ResponseEntity<ApiResponse<Void>> success() {
        ApiResponse<Void> body = ApiResponse.<Void>builder()
                .errorCode(null)
                .message(SUCCESS_MESSAGE)
                .data(null)
                .build();
        return ResponseEntity.ok(body);
    }


    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        ApiResponse<T> body = ApiResponse.<T>builder()
                .errorCode(null)
                .message(SUCCESS_MESSAGE)
                .data(data)
                .build();
        return ResponseEntity.ok(body);
    }


    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String errorCode,
            String message) {
        ApiResponse<T> body = ApiResponse.<T>builder()
                .errorCode(errorCode)
                .message(message)
                .data(null)
                .build();
        return ResponseEntity.status(status).body(body);
    }

}
