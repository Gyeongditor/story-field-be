package com.gyeongditor.storyfield.dto;

import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.response.SuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor  // Jackson이 필요로 함
@AllArgsConstructor // 생성자 기반 주입도 가능
public class ApiResponseDTO<T> {
    private int status;        // HTTP 상태 코드
    private String code;       // 내부 커스텀 코드
    private String message;    // 설명 메시지
    private T data;            // 응답 데이터

    // 성공 응답
    public static <T> ApiResponseDTO<T> success(SuccessCode successCode, T data) {
        return new ApiResponseDTO<>(
                successCode.getStatus().value(),
                successCode.getCode(),
                successCode.getMessage(),
                data
        );
    }

    // 실패 응답 (ErrorCode 기반)
    public static <T> ApiResponseDTO<T> error(ErrorCode errorCode) {
        return new ApiResponseDTO<>(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                null
        );
    }

    // 실패 응답 (Custom Message)
    public static <T> ApiResponseDTO<T> error(ErrorCode errorCode, String customMessage) {
        return new ApiResponseDTO<>(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                customMessage != null ? customMessage : errorCode.getMessage(),
                null
        );
    }
}