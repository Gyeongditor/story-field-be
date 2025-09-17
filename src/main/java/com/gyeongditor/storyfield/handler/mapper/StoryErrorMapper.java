package com.gyeongditor.storyfield.handler.mapper;

import com.gyeongditor.storyfield.response.ErrorCode;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class StoryErrorMapper {

    private static final Map<Class<? extends Throwable>, MappedError> MAPPINGS = new LinkedHashMap<>();

    static {
        // 잘못된 요청
        MAPPINGS.put(IllegalArgumentException.class,
                new MappedError(ErrorCode.STORY_400_001, "스토리 요청 데이터가 잘못되었습니다."));

        // 파일 접근 권한 문제
        MAPPINGS.put(SecurityException.class,
                new MappedError(ErrorCode.STORY_500_004, "이미지 파일 접근 권한이 없습니다."));

        // 서버 리소스 부족
        MAPPINGS.put(OutOfMemoryError.class,
                new MappedError(ErrorCode.SERVER_500_001, "서버 리소스가 부족합니다."));
    }

    public MappedError map(Throwable ex) {
        // 1) 클래스 기반 매핑
        for (var entry : MAPPINGS.entrySet()) {
            if (entry.getKey().isAssignableFrom(ex.getClass())) {
                return entry.getValue();
            }
        }

        // 2) IOException 세부 메시지 기반 처리
        if (ex instanceof IOException ioEx) {
            String msg = ioEx.getMessage();
            if (msg != null) {
                if (msg.contains("gzip 해제 실패")) {
                    return new MappedError(ErrorCode.STORY_400_002, "압축 파일 해제에 실패했습니다.");
                }
                if (msg.contains("형식") || msg.contains("format")) {
                    return new MappedError(ErrorCode.STORY_400_003, "이미지 파일 형식이 올바르지 않습니다.");
                }
                if (msg.contains("크기") || msg.contains("size")) {
                    return new MappedError(ErrorCode.STORY_413_001, "스토리 이미지 파일 크기가 너무 큽니다.");
                }
                if (msg.contains("썸네일")) {
                    return new MappedError(ErrorCode.STORY_500_001, "썸네일 업로드 중 오류가 발생했습니다.");
                }
                if (msg.contains("페이지 이미지")) {
                    return new MappedError(ErrorCode.STORY_500_002, "스토리 페이지 이미지 업로드 중 오류가 발생했습니다.");
                }
            }
            return new MappedError(ErrorCode.FILE_500_001, "파일 업로드 중 오류가 발생했습니다.");
        }

        // 3) 기본값
        return new MappedError(ErrorCode.ETC_520_001, "알 수 없는 스토리 처리 오류");
    }
}
