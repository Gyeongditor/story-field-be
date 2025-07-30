package com.gyeongditor.storyfield.handler;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.response.SuccessCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // ResponseEntity는 제외 (직접 ResponseEntity 반환한 경우는 건들지 않음)
        return !returnType.getParameterType().equals(ResponseEntity.class);
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  org.springframework.http.server.ServerHttpRequest request,
                                  org.springframework.http.server.ServerHttpResponse response) {

        // 이미 ApiResponse 형태라면 그대로 반환
        if (body instanceof ApiResponseDTO) {
            return body;
        }

        // HTTP 메서드 가져오기
        String method = request.getMethod().name();

        // 상태 코드 및 SuccessCode 결정
        HttpStatus status;
        SuccessCode successCode;

        switch (method) {
            case "POST" -> {
                status = HttpStatus.CREATED; // 201
                successCode = SuccessCode.SUCCESS_201_001;
            }
            case "DELETE" -> {
                status = HttpStatus.NO_CONTENT; // 204
                successCode = SuccessCode.SUCCESS_204_001;
            }
            case "PUT", "PATCH" -> {
                status = HttpStatus.OK; // 200
                successCode = SuccessCode.SUCCESS_200_001; // 업데이트 성공
            }
            case "GET" -> {
                status = HttpStatus.OK; // 200
                successCode = SuccessCode.SUCCESS_200_001; // 조회 성공
            }
            default -> {
                status = HttpStatus.OK;
                successCode = SuccessCode.SUCCESS_200_001;
            }
        }

        // 상태 코드 적용
        response.setStatusCode(status);

        // 204(No Content)는 body가 없어야 함
        if (status == HttpStatus.NO_CONTENT) {
            return null;
        }

        // ApiResponse.success로 감싸서 반환
        return ApiResponseDTO.success(successCode, body);
    }

    /**
     * CustomException 처리
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleCustomException(CustomException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        log.warn("[CustomException] code={}, message={}", errorCode.getCode(), ex.getCustomMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponseDTO.error(errorCode, ex.getCustomMessage()));
    }

    /**
     * 예상치 못한 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleException(Exception ex) {
        ErrorCode errorCode = ErrorCode.ETC_520_001;
        log.error("[UnhandledException] message={}", ex.getMessage(), ex);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponseDTO.error(errorCode));
    }
}
