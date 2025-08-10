package com.gyeongditor.storyfield.handler.mapper;

import com.gyeongditor.storyfield.response.ErrorCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class AuthErrorMapper {

    public MappedError map(Exception ex) {
        if (ex instanceof BadCredentialsException) {
            return new MappedError(ErrorCode.AUTH_401_009, "아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        if (ex instanceof LockedException) {
            return new MappedError(ErrorCode.USER_423_001, "비밀번호 연속 오류로 계정이 잠겼습니다.");
        }
        if (ex instanceof DisabledException) {
            return new MappedError(ErrorCode.USER_403_003, "계정이 활성화되지 않았습니다. 이메일 인증을 완료해주세요.");
        }
        if (ex instanceof CredentialsExpiredException) {
            return new MappedError(ErrorCode.AUTH_401_005, "토큰 만료");
        }
        if (ex instanceof InsufficientAuthenticationException) {
            return new MappedError(ErrorCode.AUTH_401_001, "로그인이 필요합니다.");
        }
        if (ex instanceof AuthenticationException) {
            return new MappedError(ErrorCode.AUTH_401_004, "토큰 유효하지 않음");
        }
        if (ex instanceof AccessDeniedException) {
            return new MappedError(ErrorCode.AUTH_403_002, "접근 권한 없음");
        }
        return new MappedError(ErrorCode.ETC_520_001, "알 수 없는 인증 오류");
    }
}