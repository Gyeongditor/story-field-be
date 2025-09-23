package com.gyeongditor.storyfield.handler.mapper;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.gyeongditor.storyfield.response.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class FileErrorMapper {

    // 구체 → 일반 순서 유지 (LinkedHashMap)
    private static final Map<Class<? extends Exception>, MappedError> MAP = new LinkedHashMap<>();

    static {
        // 업로드 용량 초과 등 요청 자체 문제 → 스토리 전용 에러 코드로 변경
        MAP.put(MaxUploadSizeExceededException.class,
                new MappedError(ErrorCode.STORY_413_001, "스토리 이미지 파일 크기가 너무 큽니다"));

        // 멀티파트 파싱/형식 문제 (경우에 따라 RequestErrorMapper로 보내도 됨)
        MAP.put(MultipartException.class,
                new MappedError(ErrorCode.STORY_400_002, "압축 파일 형식이 올바르지 않습니다"));

        // I/O 오류 (스트림 읽기/쓰기 실패 등) → 스토리 전용 에러 코드로 변경
        MAP.put(IOException.class,
                new MappedError(ErrorCode.STORY_500_002, "스토리 이미지 업로드 중 오류가 발생했습니다"));

        // S3/AWS 계열 오류 (Presign/통신/퍼미션 등)
        MAP.put(AmazonS3Exception.class,
                new MappedError(ErrorCode.FILE_500_002, "S3 요청 처리 중 오류가 발생했습니다."));
        MAP.put(AmazonServiceException.class,
                new MappedError(ErrorCode.FILE_500_002, "AWS 서비스 오류가 발생했습니다."));
        MAP.put(AmazonClientException.class,
                new MappedError(ErrorCode.FILE_500_002, "Presigned URL 생성 실패 또는 S3 통신 오류"));
    }

    public MappedError map(Exception ex) {
        Class<?> exClass = ex.getClass();
        for (Map.Entry<Class<? extends Exception>, MappedError> e : MAP.entrySet()) {
            if (e.getKey().isAssignableFrom(exClass)) {
                return e.getValue();
            }
        }
        // 기본값
        return new MappedError(ErrorCode.FILE_500_001, "파일 처리 중 오류");
    }
}
