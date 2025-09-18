package com.gyeongditor.storyfield.handler.mapper;

import com.gyeongditor.storyfield.response.ErrorCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class AuthErrorMapper {

    private static final Map<Class<? extends Exception>, MappedError> MAPPINGS = new LinkedHashMap<>();

    static {
        // 내부 인증 서비스 예외 (대개 UsernameNotFoundException을 감쌈)
        MAPPINGS.put(InternalAuthenticationServiceException.class,
                new MappedError(ErrorCode.USER_404_002, "해당 이메일을 가진 사용자를 찾을 수 없습니다."));

        // 로그인 자격 오류(아이디/비번 불일치) — 동일 메시지/코드
        MAPPINGS.put(BadCredentialsException.class,
                new MappedError(ErrorCode.AUTH_401_009, "아이디 또는 비밀번호가 올바르지 않습니다."));
        MAPPINGS.put(UsernameNotFoundException.class,
                new MappedError(ErrorCode.AUTH_401_009, "아이디 또는 비밀번호가 올바르지 않습니다."));
        MAPPINGS.put(ProviderNotFoundException.class,
                new MappedError(ErrorCode.AUTH_401_009, "아이디 또는 비밀번호가 올바르지 않습니다."));

        //  계정 상태 이슈
        MAPPINGS.put(LockedException.class,
                new MappedError(ErrorCode.USER_423_001, "비밀번호 연속 오류로 계정이 잠겼습니다."));
        MAPPINGS.put(DisabledException.class,
                new MappedError(ErrorCode.USER_403_003, "계정이 활성화되지 않았습니다. 이메일 인증을 완료해주세요."));
        MAPPINGS.put(AccountExpiredException.class,
                new MappedError(ErrorCode.USER_410_001, "회원 탈퇴되었거나 만료된 계정입니다."));
        MAPPINGS.put(CredentialsExpiredException.class,
                new MappedError(ErrorCode.AUTH_401_005, "토큰 만료"));

        // 인증 자격 부족(미인증)
        MAPPINGS.put(InsufficientAuthenticationException.class,
                new MappedError(ErrorCode.AUTH_401_001, "로그인이 필요합니다."));

        // 인가 실패
        MAPPINGS.put(AccessDeniedException.class,
                new MappedError(ErrorCode.AUTH_403_002, "접근 권한 없음"));

        // 그 외 AuthenticationException 전반(디폴트)
        MAPPINGS.put(AuthenticationException.class,
                new MappedError(ErrorCode.AUTH_401_004, "토큰 유효하지 않음"));
    }

    public MappedError map(Exception ex) {
        // InternalAuthenticationServiceException 특수 처리: cause가 UsernameNotFoundException이면 404 코드 유지
        if (ex instanceof InternalAuthenticationServiceException iae) {
            Throwable cause = iae.getCause();
            if (cause instanceof UsernameNotFoundException) {
                return new MappedError(ErrorCode.USER_404_002, "해당 이메일을 가진 사용자를 찾을 수 없습니다.");
            }
             return new MappedError(ErrorCode.AUTH_401_009, "아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        Class<?> exClass = ex.getClass();
        for (var entry : MAPPINGS.entrySet()) {
            if (entry.getKey().isAssignableFrom(exClass)) {
                return entry.getValue();
            }
        }
        return new MappedError(ErrorCode.ETC_520_001, "알 수 없는 인증 오류");
    }
}
