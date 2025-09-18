package com.gyeongditor.storyfield.handler.mapper;

import com.gyeongditor.storyfield.response.ErrorCode;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class PolicyErrorMapper {

    private final Map<Class<? extends Exception>, MappedError> MAPPINGS = new LinkedHashMap<>();

    public PolicyErrorMapper() {
        // CSRF 검증 실패
        MAPPINGS.put(CsrfException.class,
                new MappedError(ErrorCode.SEC_403_002, "CSRF 검증 실패"));
        
        // Too Many Requests (Rate Limiting)
        MAPPINGS.put(HttpClientErrorException.TooManyRequests.class,
                new MappedError(ErrorCode.REQ_429_001, "요청 횟수 초과"));
    }

    public MappedError map(Exception ex) {
        for (var entry : MAPPINGS.entrySet()) {
            if (entry.getKey().isInstance(ex)) {
                return entry.getValue();
            }
        }
        
        // 예외 메시지로 특정 상황 판단
        String message = ex.getMessage();
        if (message != null) {
            if (message.contains("IP") && message.contains("차단")) {
                return new MappedError(ErrorCode.SEC_403_003, "보안 정책 위반 (IP 차단)");
            }
            if (message.contains("2FA") || message.contains("이중 인증")) {
                return new MappedError(ErrorCode.SEC_401_001, "이중 인증(2FA) 미완료");
            }
        }
        
        return new MappedError(ErrorCode.SEC_403_001, "비정상 요청 탐지");
    }
}
