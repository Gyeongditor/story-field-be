package com.gyeongditor.storyfield.handler.mapper;

import com.gyeongditor.storyfield.response.ErrorCode;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import java.util.concurrent.TimeoutException;

@Component
public class ServerErrorMapper {

    public MappedError map(Exception ex) {
        if (ex instanceof DataAccessResourceFailureException) {
            return new MappedError(ErrorCode.SERVER_500_002, "DB 연결 실패");
        }
        if (ex instanceof TimeoutException || ex instanceof ResourceAccessException) {
            return new MappedError(ErrorCode.SERVER_504_001, "작업 타임아웃");
        }
        if (ex instanceof HttpServerErrorException.BadGateway) {
            return new MappedError(ErrorCode.SERVER_502_001, "외부 API 통신 오류");
        }
        if (ex instanceof RestClientException) {
            return new MappedError(ErrorCode.SERVER_502_001, "외부 API 통신 오류");
        }
        return new MappedError(ErrorCode.SERVER_500_001, "내부 서버 오류");
    }
}
