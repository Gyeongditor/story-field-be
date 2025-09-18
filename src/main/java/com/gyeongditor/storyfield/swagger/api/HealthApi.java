package com.gyeongditor.storyfield.swagger.api;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.response.SuccessCode;
import com.gyeongditor.storyfield.swagger.config.ApiErrorResponse;
import com.gyeongditor.storyfield.swagger.config.ApiSuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Health", description = "헬스체크")
@RequestMapping("/api/health")
public interface HealthApi {

    @Operation(
            summary = "헬스 체크",
            description = "서버가 정상적으로 동작 중인지 확인합니다. 인증이 필요하지 않습니다."
    )
    @ApiSuccessResponse(
            SuccessCode.SUCCESS_200_001
    )
    @ApiErrorResponse({
            ErrorCode.SERVER_500_001, // 내부 서버 오류
            ErrorCode.SERVER_503_001  // 서버 과부하
    })
    @GetMapping()
    ApiResponseDTO<String> ping();
}
