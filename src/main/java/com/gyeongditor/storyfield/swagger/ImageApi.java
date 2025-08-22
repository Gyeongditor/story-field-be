package com.gyeongditor.storyfield.swagger;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.response.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Image", description = "이미지")
@RequestMapping("/images")
public interface ImageApi {

    @Operation(
            summary = "이미지 URL 조회",
            description = "S3에 업로드된 파일명을 통해 정적 URL을 반환합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiErrorExample({
            ErrorCode.AUTH_401_012, // 유효하지 않은 인증 토큰
            ErrorCode.FILE_500_003  // 파일 URL 조회 실패
    })
    @GetMapping("/{fileName}")
    ApiResponseDTO<String> getImageUrl(
            @Parameter(description = "S3에 저장된 파일명", required = true) @PathVariable String fileName,
            @Parameter(description = "Bearer AccessToken", required = true) HttpServletRequest request
    );

    @Operation(
            summary = "이미지 삭제",
            description = "S3에 업로드된 파일명을 기반으로 이미지를 삭제합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiErrorExample({
            ErrorCode.AUTH_401_012, // 유효하지 않은 인증 토큰
            ErrorCode.FILE_500_004  // 파일 삭제 실패
    })
    @DeleteMapping("/{fileName}")
    ApiResponseDTO<Void> deleteImage(
            @Parameter(description = "삭제할 파일명", required = true) @PathVariable String fileName,
            @Parameter(description = "Bearer AccessToken", required = true) HttpServletRequest request
    );
}
