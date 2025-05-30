package com.likelion.backendplus4.talkpick.batch.common.exception.handler;

import static com.likelion.backendplus4.talkpick.batch.common.exception.error.GlobalErrorCode.*;

import com.likelion.backendplus4.talkpick.batch.common.exception.CustomException;
import com.likelion.backendplus4.talkpick.batch.common.exception.error.ErrorCode;
import com.likelion.backendplus4.talkpick.batch.common.response.ApiResponse;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.elasticsearch.BulkFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 전역 예외 처리 클래스
 * 컨트롤러에서 발생한 예외를 공통적으로 처리한다.
 *
 * @modified 2025-05-09
 * @since 2025-05-09
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * CustomException 처리
     * ErrorCode 인터페이스 기반으로 확장 가능한 방식으로 처리한다.
     *
     * @param ex CustomException 객체
     * @return 에러 응답
     * @author 정안식
     * @modified 2025-05-09 박찬병
     * @since 2025-05-09
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        return buildErrorResponse(
                errorCode.httpStatus(),
                errorCode.codeNumber(),
                errorCode.message(),
                ex
        );
    }

    /**
     * IllegalArgumentException 처리
     * 잘못된 파라미터에 대한 예외 응답 처리
     *
     * @param ex 예외 객체
     * @return 에러 응답
     * @author 정안식
     * @modified 2025-05-09 박찬병
     * @since 2025-05-09
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ILLEGAL_ARGUMENT_CODE.getCode(),
                ex.getMessage(),
                ex
        );
    }

    /**
     * MethodArgumentNotValidException 처리
     * 유효성 검사 실패에 대한 응답 처리
     *
     * @param ex 예외 객체
     * @return 에러 응답
     * @author 정안식
     * @modified 2025-05-09 박찬병
     * @since 2025-05-09
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String errorMessage = getErrorMessage(ex);
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                METHOD_ARGUMENT_NOT_VALID_CODE.getCode(),
                errorMessage,
                ex
        );
    }

    /**
     * BindException 처리
     * 폼 바인딩 유효성 실패 시 처리
     *
     * @param ex BindException 오류
     * @return 에러 응답
     * @author 박찬병
     * @modified 2025-05-09 박찬병
     * @since 2025-05-09
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException ex) {
        String errorMessage = getErrorMessage(ex);
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                BIND_EXCEPTION_CODE.getCode(),
                errorMessage,
                ex
        );
    }

    /**
     * NoHandlerFoundException 처리 메서드
     *
     * 클라이언트가 존재하지 않는 URL 경로로 요청했을 때 발생하는
     * NoHandlerFoundException을 잡아 404 Not Found 응답을 반환합니다.
     *
     * @param ex 요청한 경로에 매핑된 핸들러가 없음을 나타내는 예외
     * @return HTTP 404 상태와 표준화된 에러 페이로드를 담은 ResponseEntity
     * @author 박찬병
     * @modified 2025-05-09 박찬병
     * @since 2025-05-09
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandler(NoHandlerFoundException ex) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                NOT_FOUND_CODE.getCode(),
                "요청하신 경로를 찾을 수 없습니다.",
                ex
        );
    }

    /**
     * Elasticsearch Bulk 저장 실패 처리 핸들러
     *
     * 대량 색인(Bulk Index) 작업 중 발생한 오류에 대해,
     * 실패한 각 문서의 ID와 상태, 에러 메시지를 로그로 출력한다.
     *
     * Elasticsearch 오류는 디버깅을 위해 내부 에러 내용을 상세히 보여줄 필요가 있기 때문에,
     * 개별 실패 항목들을 모두 로그에 남기도록 구현하였다.
     *
     * @param ex Bulk 저장 도중 발생한 예외
     * @return 내부 서버 오류에 해당하는 API 응답
     * @author 함예정
     * @since 2025-05-29
     */
    @ExceptionHandler(BulkFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleElasticsearchBulkSaveException(BulkFailureException ex) {
        ex.getFailedDocuments().forEach((id, failure) -> {
            log.error("Failed to index id={} status={} \n == ERROR == \n {}",
                id,
                failure.status(),
                failure.errorMessage());
        });
        return buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            INTERNAL_SERVER_ERROR_CODE.getCode(),
            "Elastic Search Bulk Save 오류입니다.",
            ex
        );
    }

    /**
     * 기타 모든 예외 처리
     * 정의되지 않은 예외는 내부 서버 오류로 응답
     *
     * @param ex 예외 객체
     * @return 에러 응답
     * @author 정안식
     * @modified 2025-05-09 박찬병
     * @since 2025-05-09
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllExceptions(Exception ex) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                INTERNAL_SERVER_ERROR_CODE.getCode(),
                "알 수 없는 오류가 발생했습니다.",
                ex
        );
    }

    /**
     * 공통 에러 응답 생성 메서드
     * 예외 로깅 후 ApiResponse.error를 통해 표준화된 에러 응답을 생성한다.
     *
     * @param status    HTTP 상태 코드
     * @param errorCode 에러 코드 (정수형)
     * @param message   에러 메시지
     * @param ex        발생한 예외 객체
     * @return ResponseEntity<ApiResponse <Void>> 형태의 에러 응답
     * @author 박찬병
     * @modified 2025-05-09 박찬병
     * @since 2025-05-09
     */
    private ResponseEntity<ApiResponse<Void>> buildErrorResponse(
            HttpStatus status,
            int errorCode,
            String message,
            Throwable ex
    ) {
        log.error("{}: {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return ApiResponse.error(status, String.valueOf(errorCode), message);
    }

    /**
     * BindingResult 분석 후 필드별 오류 메시지 조합
     *
     * @param ex BindException 또는 MethodArgumentNotValidException 객체
     * @return 필드명과 메시지를 콤마로 연결한 오류 문자열
     * @author 박찬병
     * @modified 2025-05-09 박찬병
     * @since 2025-05-09
     */
    private String getErrorMessage(BindException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
    }
}
