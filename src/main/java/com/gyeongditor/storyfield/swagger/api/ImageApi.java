package com.gyeongditor.storyfield.swagger.api;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.response.SuccessCode;
import com.gyeongditor.storyfield.swagger.config.ApiErrorResponse;
import com.gyeongditor.storyfield.swagger.config.ApiSuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Image", description = "이미지")
@RequestMapping("/images")
public interface ImageApi {

    @Operation(
            summary = "이미지 업로드",
            description = "S3에 이미지를 업로드 합니다. .gz 압축 파일도 지원하며, 자동으로 압축 해제 후 PNG로 업로드됩니다.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiSuccessResponse(
            SuccessCode.FILE_200_001
    )
    @ApiErrorResponse({
            ErrorCode.AUTH_401_012, // 유효하지 않은 인증 토큰
            ErrorCode.STORY_400_003, // 이미지 파일 형식이 올바르지 않습니다.
            ErrorCode.STORY_400_002, // GZIP 압축 형식 오류
            ErrorCode.FILE_400_001, // 파일이 비어있음.
            ErrorCode.FILE_413_002, // 허용된 파일 크기 초과.
            ErrorCode.FILE_500_001 // 파일 업로드 실패
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponseDTO<List<String>> uploadImageFile(
            @Parameter(description = "업로드할 이미지 파일들", required = true)
            @RequestPart("files") List<MultipartFile> files,
            @Parameter(description = "Bearer AccessToken", required = true)
            HttpServletRequest request
    );

    @Operation(
            summary = "이미지 Presigned URL 조회",
            description = "S3에 업로드된 파일명을 통해 임시 접근 가능한 Presigned URL을 반환합니다. (유효기간: 10분)",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiSuccessResponse(
            SuccessCode.FILE_200_002
    )
    @ApiErrorResponse({
            ErrorCode.AUTH_401_012 // 유효하지 않은 인증 토큰
    })
    @GetMapping("/presigned-url")
    ApiResponseDTO<String> getImagePresignedUrl(
            @Parameter(description = "S3에 저장된 파일명", required = true) @RequestParam String fileName,
            @Parameter(description = "Bearer AccessToken", required = true) HttpServletRequest request
    );

    @Operation(
            summary = "이미지 URL 조회",
            description = "S3에 업로드된 파일명을 통해 정적 URL을 반환합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiSuccessResponse(
            SuccessCode.FILE_200_003
    )
    @ApiErrorResponse({
            ErrorCode.AUTH_401_012, // 유효하지 않은 인증 토큰
            ErrorCode.FILE_500_003  // 파일 URL 조회 실패
    })
    @GetMapping("/{fileName}/url")
    ApiResponseDTO<String> getImageUrl(
            @Parameter(description = "S3에 저장된 파일명", required = true) @PathVariable String fileName,
            @Parameter(description = "Bearer AccessToken", required = true) HttpServletRequest request
    );

    @Operation(
            summary = "이미지 삭제",
            description = "S3에 업로드된 파일명을 기반으로 이미지를 삭제합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiSuccessResponse(
            SuccessCode.FILE_204_001
    )
    @ApiErrorResponse({
            ErrorCode.AUTH_401_012, // 유효하지 않은 인증 토큰
            ErrorCode.FILE_500_004  // 파일 삭제 실패
    })
    @DeleteMapping("/{fileName}")
    ApiResponseDTO<Void> deleteImage(
            @Parameter(description = "삭제할 파일명", required = true) @PathVariable String fileName,
            @Parameter(description = "Bearer AccessToken", required = true) HttpServletRequest request
    );
}
