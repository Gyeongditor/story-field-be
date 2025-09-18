package com.gyeongditor.storyfield.handler.mapper;

import com.gyeongditor.storyfield.response.ErrorCode;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class StoryErrorMapper {

    private static final Map<Class<? extends Exception>, MappedError> MAPPINGS = new LinkedHashMap<>();

    static {
        // IO 계열
        MAPPINGS.put(IOException.class,
                new MappedError(ErrorCode.FILE_500_001, "파일 처리 중 오류가 발생했습니다."));

        // 잘못된 요청 (ex: 페이지/파일 불일치)
        MAPPINGS.put(IllegalArgumentException.class,
                new MappedError(ErrorCode.STORY_400_001, "스토리 요청 데이터가 유효하지 않습니다."));
    }

    public MappedError map(Exception ex) {
        Class<?> exClass = ex.getClass();
        for (var entry : MAPPINGS.entrySet()) {
            if (entry.getKey().isAssignableFrom(exClass)) {
                return entry.getValue();
            }
        }
        return new MappedError(ErrorCode.ETC_520_001, "알 수 없는 스토리 처리 오류");
    }
}
