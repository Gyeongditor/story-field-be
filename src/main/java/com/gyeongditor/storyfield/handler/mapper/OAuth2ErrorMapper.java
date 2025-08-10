package com.gyeongditor.storyfield.handler.mapper;

import com.gyeongditor.storyfield.response.ErrorCode;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class OAuth2ErrorMapper {
    public MappedError map(OAuth2AuthenticationException ex) {
        // 기본적으로 사용자 정보 조회 실패로 매핑
        return new MappedError(ErrorCode.AUTH_401_007, "OAuth2 사용자 정보를 가져올 수 없습니다.");
    }
}
