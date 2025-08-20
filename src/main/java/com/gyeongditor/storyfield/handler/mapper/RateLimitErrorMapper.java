package com.gyeongditor.storyfield.handler.mapper;

import com.gyeongditor.storyfield.response.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class RateLimitErrorMapper {

    private static final Map<Class<? extends Exception>, MappedError> MAPPINGS = new LinkedHashMap<>();

    static {
        // Rate Limiting 관련
        MAPPINGS.put(HttpClientErrorException.TooManyRequests.class,
                new MappedError(ErrorCode.REQ_429_001, "요청 횟수 초과 (Rate Limit)"));
    }

    public MappedError map(Exception ex) {
        for (var entry : MAPPINGS.entrySet()) {
            if (entry.getKey().isInstance(ex)) {
                return entry.getValue();
            }
        }
        
        // 예외 메시지로 Rate Limit 판단
        String message = ex.getMessage();
        if (message != null) {
            if (message.contains("rate limit") || message.contains("too many requests") || 
                message.contains("요청 초과") || message.contains("제한")) {
                return new MappedError(ErrorCode.REQ_429_001, "요청 횟수 초과 (Rate Limit)");
            }
        }
        
        return null; // 이 Mapper에서 처리하지 않음
    }
}
