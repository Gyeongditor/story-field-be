package com.gyeongditor.storyfield.handler.mapper;

import com.gyeongditor.storyfield.response.ErrorCode;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Component
public class RequestErrorMapper {

    public MappedError map(Exception ex) {
        if (ex instanceof MissingServletRequestParameterException) {
            return new MappedError(ErrorCode.REQ_400_002, "필수 입력값 누락");
        }
        if (ex instanceof MethodArgumentNotValidException || ex instanceof BindException) {
            return new MappedError(ErrorCode.REQ_422_001, "데이터 유효성 검사 실패");
        }
        if (ex instanceof MethodArgumentTypeMismatchException || ex instanceof HttpMessageNotReadableException) {
            return new MappedError(ErrorCode.REQ_400_003, "데이터 형식 오류");
        }
        if (ex instanceof HttpRequestMethodNotSupportedException) {
            return new MappedError(ErrorCode.REQ_405_001, "허용되지 않는 메서드");
        }
        if (ex instanceof HttpMediaTypeNotSupportedException) {
            return new MappedError(ErrorCode.REQ_415_001, "지원하지 않는 Content-Type");
        }
        if (ex instanceof IllegalArgumentException) {
            return new MappedError(ErrorCode.REQ_400_001, ex.getMessage());
        }
        return new MappedError(ErrorCode.REQ_400_001, "잘못된 요청");
    }
}
