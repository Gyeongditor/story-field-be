package com.gyeongditor.storyfield.handler.mapper;

import com.gyeongditor.storyfield.response.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Component
public class ResourceErrorMapper {

    public MappedError map(Exception ex) {
        if (ex instanceof EntityNotFoundException) {
            return new MappedError(ErrorCode.RES_404_001, "존재하지 않는 데이터");
        }
        if (ex instanceof OptimisticLockingFailureException) {
            return new MappedError(ErrorCode.RES_409_002, "동시성 충돌 (다른 사용자가 이미 수정)");
        }
        if (ex instanceof DataIntegrityViolationException) {
            return new MappedError(ErrorCode.RES_409_001, "이미 존재하는 데이터 또는 제약 조건 위반");
        }
        return new MappedError(ErrorCode.RES_404_001, "리소스를 찾을 수 없습니다.");
    }
}
