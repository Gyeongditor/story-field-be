package com.gyeongditor.storyfield.handler.mapper;

import com.amazonaws.AmazonClientException;
import com.gyeongditor.storyfield.response.ErrorCode;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FileErrorMapper {

    public MappedError map(Exception ex) {
        if (ex instanceof IOException) {
            // 업로드 과정 I/O 문제
            return new MappedError(ErrorCode.FILE_500_001, "파일 업로드 중 오류 발생");
        }
        if (ex instanceof AmazonClientException) {
            // Presigned URL, S3 통신 전반
            return new MappedError(ErrorCode.FILE_500_002, "Presigned URL 생성 실패 또는 S3 통신 오류");
        }
        return new MappedError(ErrorCode.FILE_500_001, "파일 처리 중 오류");
    }
}
