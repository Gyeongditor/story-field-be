package com.gyeongditor.storyfield.handler.mapper;

import com.gyeongditor.storyfield.response.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class FallbackErrorMapper {
    public MappedError map(Exception ex) {
        return new MappedError(ErrorCode.ETC_520_001, "알 수 없는 오류");
    }
}
