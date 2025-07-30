package com.gyeongditor.storyfield.dto;

import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.response.SuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private int status;        // HTTP 상태 코드
    private String code;       // 내부 커스텀 코드
    private String message;    // 설명 메시지
    private T data;            // 응답 데이터

    // 성공 응답
    public static <T> ApiResponse<T> success(SuccessCode successCode, T data) {
        return new ApiResponse<>(
                successCode.getStatus().value(),
                successCode.getCode(),
                successCode.getMessage(),
                data
        );
    }

    // 실패 응답 (ErrorCode 기반)
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                null
        );
    }

    // 실패 응답 (Custom Message)
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String customMessage) {
        return new ApiResponse<>(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                customMessage != null ? customMessage : errorCode.getMessage(),
                null
        );
    }
}