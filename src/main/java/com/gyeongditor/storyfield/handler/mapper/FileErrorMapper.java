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
        // 업로드 용량 초과 등 요청 자체 문제
        MAP.put(MaxUploadSizeExceededException.class,
                new MappedError(ErrorCode.REQ_413_001, "허용된 요청 크기를 초과했습니다."));

        // 멀티파트 파싱/형식 문제 (경우에 따라 RequestErrorMapper로 보내도 됨)
        MAP.put(MultipartException.class,
                new MappedError(ErrorCode.REQ_400_001, "잘못된 업로드 요청 형식입니다."));

        // I/O 오류 (스트림 읽기/쓰기 실패 등)
        MAP.put(IOException.class,
                new MappedError(ErrorCode.FILE_500_001, "파일 업로드 중 오류 발생"));

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
