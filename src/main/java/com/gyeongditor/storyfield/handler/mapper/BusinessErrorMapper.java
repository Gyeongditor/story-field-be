package com.gyeongditor.storyfield.handler.mapper;

import com.gyeongditor.storyfield.response.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class BusinessErrorMapper {

    public MappedError map(Exception ex) {
        // 기본은 서비스에서 CustomException(STORY_404_001, STORY_403_001 등)으로 던지기.
        // 혹시 도메인 전용 예외 클래스를 만들면 여기에서 → ErrorCode 매핑.
        return new MappedError(ErrorCode.ETC_520_001, "비즈니스 로직 오류");
    }
}
