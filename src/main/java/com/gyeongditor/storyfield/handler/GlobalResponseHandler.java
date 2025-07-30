package com.gyeongditor.storyfield.handler;

import com.gyeongditor.storyfield.dto.ApiResponse;
import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.response.SuccessCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    /**
     * 성공 응답: 컨트롤러가 반환하는 모든 데이터를 ApiResponse.success()로 감쌈
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // ResponseEntity는 제외 (커스터마이징된 응답을 그대로 유지)
        return !returnType.getParameterType().equals(ResponseEntity.class);
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  org.springframework.http.server.ServerHttpRequest request,
                                  org.springframework.http.server.ServerHttpResponse response) {

        // 이미 ApiResponse로 포장된 경우 그대로 반환
        if (body instanceof ApiResponse) {
            return body;
        }

        // null 리턴 시 기본 성공 메시지
        if (body == null) {
            response.setStatusCode(HttpStatus.OK);
            return ApiResponse.success(SuccessCode.SUCCESS_200_001, null);
        }

        // 데이터가 있는 경우
        response.setStatusCode(HttpStatus.OK);
        return ApiResponse.success(SuccessCode.SUCCESS_200_001, body);
    }

    /**
     * 커스텀 예외 처리
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomException(CustomException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        log.warn("[CustomException] code={}, message={}", errorCode.getCode(), errorCode.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode));
    }

    /**
     * 예상치 못한 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception ex) {
        ErrorCode errorCode = ErrorCode.ETC_520_001;

        log.error("[UnhandledException] message={}", ex.getMessage(), ex);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode));
    }
}
