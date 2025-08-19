package com.gyeongditor.storyfield.response;

import org.springframework.http.HttpStatus;

public enum SuccessCode {

    // 1. 공통 성공 코드
    SUCCESS_200_001(HttpStatus.OK, "SUCCESS_200_001", "요청이 성공적으로 처리되었습니다."),
    SUCCESS_201_001(HttpStatus.CREATED, "SUCCESS_201_001", "리소스가 성공적으로 생성되었습니다."),
    SUCCESS_204_001(HttpStatus.NO_CONTENT, "SUCCESS_204_001", "요청이 성공적으로 처리되었으며 반환할 콘텐츠가 없습니다."),

    // 2. 사용자 관련
    USER_201_001(HttpStatus.CREATED, "USER_201_001", "회원가입이 완료되었습니다."),
    USER_200_001(HttpStatus.OK, "USER_200_001", "회원 정보 조회가 성공적으로 완료되었습니다."),
    USER_200_002(HttpStatus.OK, "USER_200_002", "회원 정보가 성공적으로 수정되었습니다."),
    USER_200_003(HttpStatus.OK, "USER_200_003", "이메일 인증이 완료되었습니다."),
    USER_204_001(HttpStatus.NO_CONTENT, "USER_204_001", "회원이 성공적으로 삭제되었습니다."),

    // 3. 인증/Auth
    AUTH_200_001(HttpStatus.OK, "AUTH_200_001", "로그인이 성공적으로 완료되었습니다."),
    AUTH_200_002(HttpStatus.OK, "AUTH_200_002", "로그아웃이 성공적으로 완료되었습니다."),
    AUTH_200_007(HttpStatus.OK, "AUTH_200_003", "RT값으로 새로운 AT생성에 성공했습니다."),

    // 4. 메일
    MAIL_200_001(HttpStatus.OK, "MAIL_200_001", "인증 메일이 성공적으로 전송되었습니다."),
    MAIL_200_002(HttpStatus.OK, "MAIL_200_002", "이메일 인증이 완료되었습니다."),

    // 5. 스토리 관련
    STORY_201_001(HttpStatus.CREATED, "STORY_201_001", "스토리가 성공적으로 생성되었습니다."),
    STORY_200_001(HttpStatus.OK, "STORY_200_001", "스토리 페이지가 성공적으로 조회되었습니다."),
    STORY_200_002(HttpStatus.OK, "STORY_200_002", "메인 페이지 스토리 목록이 성공적으로 조회되었습니다."),
    STORY_204_001(HttpStatus.NO_CONTENT, "STORY_204_001", "스토리가 성공적으로 삭제되었습니다."),
    // 6. OAuth2 인증
    OAUTH2_200_001(HttpStatus.OK, "OAUTH2_200_001", "OAuth2 로그인 인증이 성공적으로 완료되었습니다."),
    OAUTH2_201_001(HttpStatus.CREATED, "OAUTH2_201_001", "신규 OAuth2 사용자가 성공적으로 생성되었습니다."),
    // 7. 사용자 인증
    AUTH_200_003(HttpStatus.OK, "AUTH_200_003", "사용자 정보 로드가 성공적으로 완료되었습니다."),
    AUTH_200_004(HttpStatus.OK, "AUTH_200_004", "계정 상태가 정상입니다."),
    AUTH_200_005(HttpStatus.OK, "AUTH_200_005", "로그인 실패 횟수가 초기화되었습니다."),
    AUTH_200_006(HttpStatus.OK, "AUTH_200_006", "로그인 실패 횟수가 업데이트되었습니다."),

    // 8. 파일 성공
    FILE_200_001(HttpStatus.OK, "FILE_200_001", "파일 업로드 성공"),
    FILE_200_002(HttpStatus.OK, "FILE_200_002", "Presigned URL 생성 성공"),
    FILE_200_003(HttpStatus.OK, "FILE_200_003", "파일 URL 조회 성공"),
    FILE_204_001(HttpStatus.NO_CONTENT, "FILE_204_001", "파일 삭제 성공");

    // 필드
    private final HttpStatus status;
    private final String code;
    private final String message;

    SuccessCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
}
