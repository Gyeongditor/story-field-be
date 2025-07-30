package com.gyeongditor.storyfield.response;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // ===================== 1. 인증/인가 (Authentication & Authorization) =====================
    AUTH_401_001(HttpStatus.UNAUTHORIZED, "AUTH_401_001", "로그인 필요"),
    AUTH_401_002(HttpStatus.UNAUTHORIZED, "AUTH_401_002", "세션 만료"),
    AUTH_401_003(HttpStatus.UNAUTHORIZED, "AUTH_401_003", "토큰 없음"),
    AUTH_401_004(HttpStatus.UNAUTHORIZED, "AUTH_401_004", "토큰 유효하지 않음"),
    AUTH_401_005(HttpStatus.UNAUTHORIZED, "AUTH_401_005", "토큰 만료"),
    AUTH_403_001(HttpStatus.FORBIDDEN, "AUTH_403_001", "계정 정지됨"),
    AUTH_403_002(HttpStatus.FORBIDDEN, "AUTH_403_002", "접근 권한 없음"),
    AUTH_403_003(HttpStatus.FORBIDDEN, "AUTH_403_003", "해당 기능은 특정 등급 이상만 가능"),
    AUTH_401_006(HttpStatus.UNAUTHORIZED, "AUTH_401_006", "OAuth 인증 실패"),

    // ===================== 2. 사용자 입력/요청 오류 (Client Errors) =====================
    REQ_400_001(HttpStatus.BAD_REQUEST, "REQ_400_001", "잘못된 요청 형식"),
    REQ_400_002(HttpStatus.BAD_REQUEST, "REQ_400_002", "필수 입력값 누락"),
    REQ_400_003(HttpStatus.BAD_REQUEST, "REQ_400_003", "데이터 형식 오류 (숫자 → 문자열)"),
    REQ_422_001(HttpStatus.UNPROCESSABLE_ENTITY, "REQ_422_001", "데이터 유효성 검사 실패 (이메일 형식 불일치)"),
    REQ_405_001(HttpStatus.METHOD_NOT_ALLOWED, "REQ_405_001", "허용되지 않는 메서드 (GET 대신 POST)"),
    REQ_415_001(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "REQ_415_001", "지원하지 않는 Content-Type"),
    REQ_413_001(HttpStatus.PAYLOAD_TOO_LARGE, "REQ_413_001", "허용된 요청 크기 초과"),
    REQ_429_001(HttpStatus.TOO_MANY_REQUESTS, "REQ_429_001", "요청 횟수 초과 (Rate Limit)"),

    // ===================== 3. 회원/계정 관련 (Account & User Management) =====================
    USER_409_001(HttpStatus.CONFLICT, "USER_409_001", "중복된 이메일"),
    USER_409_002(HttpStatus.CONFLICT, "USER_409_002", "중복된 닉네임"),
    USER_404_001(HttpStatus.NOT_FOUND, "USER_404_001", "존재하지 않는 계정"),
    USER_401_001(HttpStatus.UNAUTHORIZED, "USER_401_001", "비밀번호 불일치"),
    USER_423_001(HttpStatus.LOCKED, "USER_423_001", "비밀번호 연속 오류 횟수 초과 (잠금)"),
    USER_410_001(HttpStatus.GONE, "USER_410_001", "회원 탈퇴된 계정"),

    // ===================== 4. 리소스 관련 (Resource Errors) =====================
    RES_404_001(HttpStatus.NOT_FOUND, "RES_404_001", "존재하지 않는 데이터"),
    RES_410_001(HttpStatus.GONE, "RES_410_001", "삭제된 데이터 접근"),
    RES_409_001(HttpStatus.CONFLICT, "RES_409_001", "이미 존재하는 데이터"),
    RES_409_002(HttpStatus.CONFLICT, "RES_409_002", "동시성 충돌 (다른 사용자가 이미 수정)"),
    RES_403_001(HttpStatus.FORBIDDEN, "RES_403_001", "데이터 조회 제한 (비공개, 제한 계정)"),

    // ===================== 5. 비즈니스 로직 오류 (Business Logic) =====================
    BIZ_409_001(HttpStatus.CONFLICT, "BIZ_409_001", "재고 부족"),
    BIZ_400_001(HttpStatus.BAD_REQUEST, "BIZ_400_001", "구매 불가 상태 (판매 종료)"),
    BIZ_403_001(HttpStatus.FORBIDDEN, "BIZ_403_001", "한도 초과 (월 최대 구매 수량)"),
    BIZ_400_002(HttpStatus.BAD_REQUEST, "BIZ_400_002", "쿠폰 사용 불가 (기간 만료)"),
    BIZ_403_002(HttpStatus.FORBIDDEN, "BIZ_403_002", "이벤트 참여 제한 (참여 횟수 초과)"),
    BIZ_402_001(HttpStatus.PAYMENT_REQUIRED, "BIZ_402_001", "결제 실패 (PG사 오류)"),
    BIZ_400_003(HttpStatus.BAD_REQUEST, "BIZ_400_003", "결제 취소"),

    // ===================== 6. 서버/인프라 오류 (Server Errors) =====================
    SERVER_500_001(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_500_001", "내부 서버 오류"),
    SERVER_500_002(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_500_002", "DB 연결 실패"),
    SERVER_502_001(HttpStatus.BAD_GATEWAY, "SERVER_502_001", "외부 API 통신 오류"),
    SERVER_503_001(HttpStatus.SERVICE_UNAVAILABLE, "SERVER_503_001", "서버 과부하"),
    SERVER_504_001(HttpStatus.GATEWAY_TIMEOUT, "SERVER_504_001", "작업 타임아웃"),
    SERVER_502_002(HttpStatus.BAD_GATEWAY, "SERVER_502_002", "캐시 서버 오류"),

    // ===================== 7. 파일/업로드 관련 (File & Upload) =====================
    FILE_413_001(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_413_001", "파일 크기 초과"),
    FILE_415_001(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "FILE_415_001", "파일 확장자 허용 안됨"),
    FILE_500_001(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_500_001", "이미지 업로드 실패 (변환 오류)"),
    FILE_500_002(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_500_002", "동영상 처리 실패 (인코딩 오류)"),

    // ===================== 8. 정책/보안 관련 (Policy & Security) =====================
    SEC_403_001(HttpStatus.FORBIDDEN, "SEC_403_001", "비정상 요청 탐지"),
    SEC_403_002(HttpStatus.FORBIDDEN, "SEC_403_002", "CSRF 검증 실패"),
    SEC_403_003(HttpStatus.FORBIDDEN, "SEC_403_003", "보안 정책 위반 (IP 차단)"),
    SEC_403_004(HttpStatus.FORBIDDEN, "SEC_403_004", "관리자 승인 대기 상태"),
    SEC_401_001(HttpStatus.UNAUTHORIZED, "SEC_401_001", "이중 인증(2FA) 미완료"),

    // ===================== 9. 기타 (Etc) =====================
    ETC_520_001(HttpStatus.valueOf(520), "ETC_520_001", "알 수 없는 오류"),
    ETC_503_001(HttpStatus.SERVICE_UNAVAILABLE, "ETC_503_001", "서비스 점검 중"),
    ETC_426_001(HttpStatus.UPGRADE_REQUIRED, "ETC_426_001", "지원하지 않는 버전");

    // ===================== Fields =====================
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

