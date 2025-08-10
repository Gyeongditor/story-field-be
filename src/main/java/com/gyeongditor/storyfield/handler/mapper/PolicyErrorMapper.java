package com.gyeongditor.storyfield.handler.mapper;

import com.gyeongditor.storyfield.response.ErrorCode;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;

@Component
public class PolicyErrorMapper {

    public MappedError map(Exception ex) {
        if (ex instanceof CsrfException) {
            return new MappedError(ErrorCode.SEC_403_002, "CSRF 검증 실패");
        }
        // 2FA 미완료 등은 서비스에서 CustomException(SEC_401_001)로 던지는 것을 권장
        return new MappedError(ErrorCode.SEC_403_001, "비정상 요청 탐지");
    }
}
