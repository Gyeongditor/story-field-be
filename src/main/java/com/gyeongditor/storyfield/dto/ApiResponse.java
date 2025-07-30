package com.gyeongditor.storyfield.dto;

import com.gyeongditor.storyfield.response.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import com.gyeongditor.storyfield.response.SuccessCode;


@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private String code;
    private String message;
    private T data;

    // 성공 응답
    public static <T> ApiResponse<T> success(SuccessCode successCode, T data) {
        return new ApiResponse<>(successCode.getCode(), successCode.getMessage(), data);
    }

    // 실패 응답
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), null);
    }
}

