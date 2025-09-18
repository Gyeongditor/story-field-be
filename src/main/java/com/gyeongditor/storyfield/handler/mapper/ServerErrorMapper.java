package com.gyeongditor.storyfield.handler.mapper;

import com.gyeongditor.storyfield.response.ErrorCode;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Component
public class ServerErrorMapper {

    private static final Map<Class<? extends Exception>, MappedError> MAPPINGS = new LinkedHashMap<>();

    static {
        // DB 연결 실패
        MAPPINGS.put(DataAccessResourceFailureException.class,
                new MappedError(ErrorCode.SERVER_500_002, "DB 연결 실패"));
        MAPPINGS.put(SQLException.class,
                new MappedError(ErrorCode.SERVER_500_002, "DB 연결 실패"));

        // 작업 타임아웃
        MAPPINGS.put(TimeoutException.class,
                new MappedError(ErrorCode.SERVER_504_001, "작업 타임아웃"));
        MAPPINGS.put(ResourceAccessException.class,
                new MappedError(ErrorCode.SERVER_504_001, "작업 타임아웃"));
        MAPPINGS.put(QueryTimeoutException.class,
                new MappedError(ErrorCode.SERVER_504_001, "작업 타임아웃"));

        // 외부 API 통신 오류
        MAPPINGS.put(HttpServerErrorException.BadGateway.class,
                new MappedError(ErrorCode.SERVER_502_001, "외부 API 통신 오류"));
        MAPPINGS.put(RestClientException.class,
                new MappedError(ErrorCode.SERVER_502_001, "외부 API 통신 오류"));

        // 서버 과부하
        MAPPINGS.put(TransientDataAccessException.class,
                new MappedError(ErrorCode.SERVER_503_001, "서버 과부하"));
        MAPPINGS.put(HttpServerErrorException.ServiceUnavailable.class,
                new MappedError(ErrorCode.SERVER_503_001, "서버 과부하"));
    }

    public MappedError map(Exception ex) {
        for (var entry : MAPPINGS.entrySet()) {
            if (entry.getKey().isInstance(ex)) {
                return entry.getValue();
            }
        }
        return new MappedError(ErrorCode.SERVER_500_001, "내부 서버 오류");
    }
}
