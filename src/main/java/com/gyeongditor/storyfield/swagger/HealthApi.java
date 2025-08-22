package com.gyeongditor.storyfield.swagger;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Health", description = "헬스체크")
@RequestMapping("/api/health")
public interface HealthApi {

    @Operation(summary = "헬스 체크", description = "간단한 200 OK 확인용 API.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "서버 정상 동작",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {"status":200,"code":"SUCCESS_200_001","message":"요청이 성공적으로 처리되었습니다.","data":"OK"}""")))
    })
    @GetMapping("/ping")
    ApiResponseDTO<String> ping();
}
