package com.gyeongditor.storyfield.response;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // 1. 인증/인가 (Authentication & Authorization)
    AUTH_401_001(HttpStatus.UNAUTHORIZED, "AUTH_401_001", "로그인 필요"),
    AUTH_401_003(HttpStatus.UNAUTHORIZED, "AUTH_401_003", "토큰 없음"),
    AUTH_401_004(HttpStatus.UNAUTHORIZED, "AUTH_401_004", "토큰 유효하지 않음"),
    AUTH_401_005(HttpStatus.UNAUTHORIZED, "AUTH_401_005", "토큰 만료"),
    AUTH_401_006(HttpStatus.UNAUTHORIZED, "AUTH_401_006", "OAuth 인증 실패"),
    AUTH_401_007(HttpStatus.UNAUTHORIZED, "AUTH_401_007", "OAuth2 사용자 정보를 가져올 수 없습니다."),
    AUTH_401_008(HttpStatus.UNAUTHORIZED, "AUTH_401_008", "지원하지 않는 OAuth2 Provider입니다."),
    AUTH_401_009(HttpStatus.UNAUTHORIZED, "AUTH_401_009", "아이디 또는 비밀번호가 올바르지 않습니다."),
    AUTH_401_010(HttpStatus.UNAUTHORIZED, "AUTH_401_010", "인증 토큰이 없습니다."),
    AUTH_401_011(HttpStatus.UNAUTHORIZED, "AUTH_401_011", "인증 토큰이 만료되었습니다."),
    AUTH_401_012(HttpStatus.UNAUTHORIZED, "AUTH_401_012", "유효하지 않은 인증 토큰입니다."),
    AUTH_401_013(HttpStatus.UNAUTHORIZED, "AUTH_401_013", "세션이 만료되었습니다."),
    AUTH_403_001(HttpStatus.FORBIDDEN, "AUTH_403_001", "계정 정지됨"),
    AUTH_403_002(HttpStatus.FORBIDDEN, "AUTH_403_002", "접근 권한 없음"),
    AUTH_403_003(HttpStatus.FORBIDDEN, "AUTH_403_003", "해당 기능은 특정 등급 이상만 가능"),
    AUTH_500_001(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_500_001", "OAuth2 사용자 정보 처리 중 오류가 발생했습니다."),

    // 2. 사용자 입력/요청 오류 (Client Errors)
    REQ_400_001(HttpStatus.BAD_REQUEST, "REQ_400_001", "잘못된 요청 형식"),
    REQ_400_002(HttpStatus.BAD_REQUEST, "REQ_400_002", "필수 입력값 누락"),
    REQ_400_003(HttpStatus.BAD_REQUEST, "REQ_400_003", "데이터 형식 오류 (숫자 → 문자열)"),
    REQ_422_001(HttpStatus.UNPROCESSABLE_ENTITY, "REQ_422_001", "데이터 유효성 검사 실패 (이메일 형식 불일치)"),
    REQ_405_001(HttpStatus.METHOD_NOT_ALLOWED, "REQ_405_001", "허용되지 않는 메서드 (GET 대신 POST)"),
    REQ_415_001(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "REQ_415_001", "지원하지 않는 Content-Type"),
    REQ_413_001(HttpStatus.PAYLOAD_TOO_LARGE, "REQ_413_001", "허용된 요청 크기 초과"),
    REQ_429_001(HttpStatus.TOO_MANY_REQUESTS, "REQ_429_001", "요청 횟수 초과 (Rate Limit)"),

    // 3. 회원/계정 관련 (Account & User Management)
    USER_409_001(HttpStatus.CONFLICT, "USER_409_001", "중복된 이메일"),
    USER_409_002(HttpStatus.CONFLICT, "USER_409_002", "이미 사용 중인 닉네임입니다."),
    USER_404_001(HttpStatus.NOT_FOUND, "USER_404_001", "존재하지 않는 계정"),
    USER_404_002(HttpStatus.NOT_FOUND, "USER_404_002", "해당 이메일을 가진 사용자를 찾을 수 없습니다."),
    USER_410_001(HttpStatus.GONE, "USER_410_001", "회원 탈퇴된 계정"),
    USER_410_002(HttpStatus.GONE, "USER_410_002", "비활성화된 소셜 계정"),
    USER_401_001(HttpStatus.UNAUTHORIZED, "USER_401_001", "비밀번호 불일치"),
    USER_423_001(HttpStatus.LOCKED, "USER_423_001", "비밀번호 연속 오류 횟수 초과 (계정 잠금)"),
    USER_423_002(HttpStatus.LOCKED, "USER_423_002", "계정이 잠금되었습니다. 잠시 후 다시 시도해주세요."),
    USER_403_003(HttpStatus.FORBIDDEN, "USER_403_003", "계정이 활성화되지 않았습니다. 이메일 인증을 완료해주세요."),

    // 4. 리소스 관련 (Resource Errors)
    RES_404_001(HttpStatus.NOT_FOUND, "RES_404_001", "존재하지 않는 데이터"),
    RES_410_001(HttpStatus.GONE, "RES_410_001", "삭제된 데이터 접근"),
    RES_409_001(HttpStatus.CONFLICT, "RES_409_001", "이미 존재하는 데이터"),
    RES_409_002(HttpStatus.CONFLICT, "RES_409_002", "동시성 충돌 (다른 사용자가 이미 수정)"),
    RES_403_001(HttpStatus.FORBIDDEN, "RES_403_001", "데이터 조회 제한 (비공개, 제한 계정)"),

    // 5. 메일 관련
    MAIL_500_001(HttpStatus.INTERNAL_SERVER_ERROR, "MAIL_500_001", "메일 전송 중 오류가 발생했습니다."),

    // 6. 서버/인프라 오류 (Server Errors)
    SERVER_500_001(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_500_001", "내부 서버 오류"),
    SERVER_500_002(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_500_002", "DB 연결 실패"),
    SERVER_502_001(HttpStatus.BAD_GATEWAY, "SERVER_502_001", "외부 API 통신 오류"),
    SERVER_503_001(HttpStatus.SERVICE_UNAVAILABLE, "SERVER_503_001", "서버 과부하"),
    SERVER_504_001(HttpStatus.GATEWAY_TIMEOUT, "SERVER_504_001", "작업 타임아웃"),

    // 7. 정책/보안 관련 (Policy & Security)
    SEC_403_001(HttpStatus.FORBIDDEN, "SEC_403_001", "비정상 요청 탐지"),
    SEC_403_002(HttpStatus.FORBIDDEN, "SEC_403_002", "CSRF 검증 실패"),
    SEC_403_003(HttpStatus.FORBIDDEN, "SEC_403_003", "보안 정책 위반 (IP 차단)"),
    SEC_403_004(HttpStatus.FORBIDDEN, "SEC_403_004", "관리자 승인 대기 상태"),
    SEC_401_001(HttpStatus.UNAUTHORIZED, "SEC_401_001", "이중 인증(2FA) 미완료"),

    // 8. 스토리/파일 관련
    STORY_400_001(HttpStatus.BAD_REQUEST, "STORY_400_001", "스토리 요청 데이터가 잘못되었습니다."),
    STORY_404_001(HttpStatus.NOT_FOUND, "STORY_404_001", "스토리를 찾을 수 없습니다."),
    STORY_403_001(HttpStatus.FORBIDDEN, "STORY_403_001", "본인 스토리만 수정/삭제할 수 있습니다."),
    FILE_400_001(HttpStatus.BAD_REQUEST, "FILE_400_001", "파일이 비어 있습니다."),
    FILE_400_002(HttpStatus.BAD_REQUEST, "FILE_400_002", "허용되지 않은 파일 형식입니다."),
    FILE_413_002(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_413_002", "허용된 파일 크기를 초과했습니다."),
    FILE_500_001(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_500_001", "파일 업로드 중 오류가 발생했습니다."),
    FILE_500_002(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_500_002", "파일 삭제 중 오류가 발생했습니다."),
    FILE_500_003(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_500_003", "파일 URL 조회 실패"),
    FILE_500_004(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_500_004", "파일 삭제 실패"),

    // 9. 기타 (Etc)
    ETC_520_001(HttpStatus.INTERNAL_SERVER_ERROR, "ETC_520_001", "알 수 없는 오류");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
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
