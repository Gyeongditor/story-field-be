package com.gyeongditor.storyfield.handler.mapper;

import com.gyeongditor.storyfield.response.ErrorCode;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class RequestErrorMapper {

    private static final Map<Class<? extends Exception>, MappedError> MAPPINGS = new LinkedHashMap<>();

    static {
        // 필수 입력값 누락
        MAPPINGS.put(MissingServletRequestParameterException.class,
                new MappedError(ErrorCode.REQ_400_002, "필수 입력값 누락"));
        MAPPINGS.put(MissingRequestHeaderException.class,
                new MappedError(ErrorCode.REQ_400_002, "필수 헤더값 누락"));
        MAPPINGS.put(MissingPathVariableException.class,
                new MappedError(ErrorCode.REQ_400_002, "필수 경로 변수 누락"));

        // 데이터 유효성 검사 실패
        MAPPINGS.put(MethodArgumentNotValidException.class,
                new MappedError(ErrorCode.REQ_422_001, "데이터 유효성 검사 실패"));
        MAPPINGS.put(BindException.class,
                new MappedError(ErrorCode.REQ_422_001, "데이터 유효성 검사 실패"));

        // 데이터 형식 오류
        MAPPINGS.put(MethodArgumentTypeMismatchException.class,
                new MappedError(ErrorCode.REQ_400_003, "데이터 형식 오류"));
        MAPPINGS.put(HttpMessageNotReadableException.class,
                new MappedError(ErrorCode.REQ_400_003, "JSON 형식 오류"));

        // HTTP 메서드 오류
        MAPPINGS.put(HttpRequestMethodNotSupportedException.class,
                new MappedError(ErrorCode.REQ_405_001, "허용되지 않는 메서드"));

        // Content-Type 오류
        MAPPINGS.put(HttpMediaTypeNotSupportedException.class,
                new MappedError(ErrorCode.REQ_415_001, "지원하지 않는 Content-Type"));

        // 파일 크기 초과
        MAPPINGS.put(MaxUploadSizeExceededException.class,
                new MappedError(ErrorCode.REQ_413_001, "허용된 요청 크기 초과"));
        MAPPINGS.put(SizeLimitExceededException.class,
                new MappedError(ErrorCode.REQ_413_001, "허용된 요청 크기 초과"));

        // 잘못된 요청 형식 (기본)
        MAPPINGS.put(IllegalArgumentException.class,
                new MappedError(ErrorCode.REQ_400_001, "잘못된 요청 형식"));
    }

    public MappedError map(Exception ex) {
        for (var entry : MAPPINGS.entrySet()) {
            if (entry.getKey().isInstance(ex)) {
                // IllegalArgumentException은 커스텀 메시지 사용
                if (ex instanceof IllegalArgumentException && ex.getMessage() != null) {
                    return new MappedError(ErrorCode.REQ_400_001, ex.getMessage());
                }
                return entry.getValue();
            }
        }
        return new MappedError(ErrorCode.REQ_400_001, "잘못된 요청");
    }
}
