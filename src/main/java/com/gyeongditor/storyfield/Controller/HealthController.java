package com.gyeongditor.storyfield.Controller;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health", description = "헬스체크")
@RequestMapping("/health")
@RestController
public class HealthController {

    @Operation(summary = "헬스 체크", description = "간단한 200 OK 확인용 API.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "서버 정상 동작")
    })
    @GetMapping("/health/ping")
    public ApiResponseDTO<String> ping() {
        return ApiResponseDTO.success(SuccessCode.SUCCESS_200_001, "OK");
    }
}
