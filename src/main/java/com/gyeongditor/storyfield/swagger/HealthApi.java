package com.gyeongditor.storyfield.swagger;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.response.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Health", description = "헬스체크")
@RequestMapping("/api/health")
public interface HealthApi {

    @Operation(summary = "헬스 체크", description = "간단한 200 OK 확인용 API.")
    @ApiErrorExample({
            ErrorCode.SERVER_500_001, // 내부 서버 오류
            ErrorCode.SERVER_503_001  // 서버 과부하
    })
    @GetMapping("/ping")
    ApiResponseDTO<String> ping();
}
