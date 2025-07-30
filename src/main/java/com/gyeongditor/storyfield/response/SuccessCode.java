package com.gyeongditor.storyfield.response;

import org.springframework.http.HttpStatus;

public enum SuccessCode {

    // ===================== 1. 공통 성공 코드 =====================
    SUCCESS_200_001(HttpStatus.OK, "SUCCESS_200_001", "요청이 성공적으로 처리되었습니다."),
    SUCCESS_201_001(HttpStatus.CREATED, "SUCCESS_201_001", "리소스가 성공적으로 생성되었습니다."),
    SUCCESS_204_001(HttpStatus.NO_CONTENT, "SUCCESS_204_001", "요청이 성공적으로 처리되었으며 반환할 콘텐츠가 없습니다."),

    // ===================== 2. 사용자 관련  =====================
    USER_200_001(HttpStatus.OK, "USER_200_001", "회원가입이 완료되었습니다."),
    USER_200_002(HttpStatus.OK, "USER_200_002", "로그인이 성공적으로 처리되었습니다."),
    USER_200_003(HttpStatus.OK, "USER_200_003", "회원 정보가 성공적으로 수정되었습니다.");


    // ===================== Fields =====================
    private final HttpStatus status;
    private final String code;
    private final String message;

    SuccessCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
