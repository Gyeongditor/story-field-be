package com.gyeongditor.storyfield.handler.mapper;

import com.gyeongditor.storyfield.response.ErrorCode;
import org.springframework.stereotype.Component;

/**
 * Audio 관련 예외를 ErrorCode로 매핑하는 컴포넌트
 * 기존 FileErrorMapper와 유사한 패턴으로 Audio 전용 에러 처리
 */
@Component
public class AudioErrorMapper {

    public MappedError map(Exception ex) {
        String message = ex.getMessage();
        
        // 파일 크기 관련
        if (message != null && message.contains("file size") || 
            message.contains("too large") || message.contains("크기")) {
            return new MappedError(ErrorCode.AUDIO_413_001, "오디오 파일 크기가 너무 큽니다.");
        }
        
        // 파일 형식 관련
        if (message != null && (message.contains("format") || 
            message.contains("type") || message.contains("형식"))) {
            return new MappedError(ErrorCode.AUDIO_400_002, "지원하지 않는 오디오 파일 형식입니다.");
        }
        
        // 파일 없음
        if (message != null && (message.contains("empty") || 
            message.contains("비어") || message.contains("null"))) {
            return new MappedError(ErrorCode.AUDIO_400_001, "오디오 파일이 비어 있습니다.");
        }
        
        // 파일 찾을 수 없음
        if (message != null && (message.contains("not found") || 
            message.contains("찾을 수 없") || message.contains("NoSuchKey"))) {
            return new MappedError(ErrorCode.AUDIO_404_001, "오디오 파일을 찾을 수 없습니다.");
        }
        
        // URL 생성 관련
        if (message != null && (message.contains("presigned") || 
            message.contains("URL") || message.contains("생성"))) {
            return new MappedError(ErrorCode.AUDIO_500_004, "Presigned URL 생성에 실패했습니다.");
        }
        
        // 삭제 관련
        if (message != null && (message.contains("delete") || message.contains("삭제"))) {
            return new MappedError(ErrorCode.AUDIO_500_002, "오디오 파일 삭제 중 오류가 발생했습니다.");
        }
        
        // 기본 오디오 업로드 오류
        return new MappedError(ErrorCode.AUDIO_500_001, "오디오 파일 처리 중 오류가 발생했습니다.");
    }
}
