package com.gyeongditor.storyfield.exception;

import com.gyeongditor.storyfield.response.ErrorCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String customMessage; // 추가 설명 (선택)

    // 기본 생성자
    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = null;
    }

    // 상세 메시지 추가 가능
    public CustomException(ErrorCode errorCode, String customMessage) {
        super(customMessage != null ? customMessage : errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }


    public String getCustomMessage() {
        return customMessage != null ? customMessage : errorCode.getMessage();
    }
}
