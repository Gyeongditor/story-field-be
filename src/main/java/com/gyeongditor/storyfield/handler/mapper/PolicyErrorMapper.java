package com.gyeongditor.storyfield.handler.mapper;

import com.gyeongditor.storyfield.response.ErrorCode;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class PolicyErrorMapper {

    private final Map<Class<? extends Exception>, MappedError> MAPPINGS = new LinkedHashMap<>();

    public PolicyErrorMapper() {
        MAPPINGS.put(CsrfException.class,
                new MappedError(ErrorCode.SEC_403_002, "CSRF 검증 실패"));
        // 2FA 미완료, 보안 정책 위반 등은 서비스 단에서 CustomException 던지기
    }

    public MappedError map(Exception ex) {
        for (var entry : MAPPINGS.entrySet()) {
            if (entry.getKey().isInstance(ex)) {
                return entry.getValue();
            }
        }
        return new MappedError(ErrorCode.SEC_403_001, "비정상 요청 탐지");
    }
}
