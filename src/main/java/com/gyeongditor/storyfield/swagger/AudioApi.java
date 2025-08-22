package com.gyeongditor.storyfield.swagger;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Audio", description = "오디오 파일 업로드 및 관리")
@RequestMapping("/api/audio")
public interface AudioApi {

    @Operation(
            summary = "오디오 업로드",
            description = "Multipart 형식으로 오디오 파일을 업로드하고, 업로드된 오디오의 S3 URL을 반환합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiSuccessResponse({
            SuccessCode.AUDIO_200_001
    })
    @ApiErrorResponse({
            ErrorCode.AUDIO_400_001,
            ErrorCode.AUDIO_400_002,
            ErrorCode.AUDIO_413_001,
            ErrorCode.AUDIO_500_001
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponseDTO<String> uploadAudio(
            @Parameter(description = "업로드할 오디오 파일 (mp3, wav, m4a 등)", required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")))
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Bearer AccessToken", required = true)
            HttpServletRequest request
    );

    @Operation(
            summary = "오디오 URL 조회",
            description = "S3에 업로드된 오디오 파일명을 통해 정적 URL을 반환합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiSuccessResponse({
            SuccessCode.AUDIO_200_003
    })
    @ApiErrorResponse({
            ErrorCode.AUDIO_404_001,
            ErrorCode.AUDIO_500_003
    })
    @GetMapping("/{fileName}")
    ApiResponseDTO<String> getAudioUrl(
            @Parameter(description = "S3에 저장된 오디오 파일명", required = true) @PathVariable String fileName,
            @Parameter(description = "Bearer AccessToken", required = true) HttpServletRequest request
    );

    @Operation(
            summary = "다운로드용 Presigned URL 발급",
            description = "private 버킷에서 오디오 파일을 GET으로 내려받기 위한 Presigned URL을 발급합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiSuccessResponse(
            SuccessCode.AUDIO_200_003
    )
    @ApiErrorResponse({
            ErrorCode.AUDIO_404_001,
            ErrorCode.AUDIO_500_004
    })
    @GetMapping("/presign/download")
    ApiResponseDTO<String> getDownloadPresignedUrl(
            @Parameter(description = "S3 오디오 키 또는 파일명", required = true) @RequestParam String keyOrFileName,
            @Parameter(description = "Bearer AccessToken", required = true) HttpServletRequest request
    );

    @Operation(
            summary = "오디오 삭제",
            description = "S3에 업로드된 오디오 파일명을 기반으로 삭제합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiSuccessResponse(
            SuccessCode.AUDIO_204_001
    )
    @ApiErrorResponse({
            ErrorCode.AUDIO_404_001,
            ErrorCode.AUDIO_500_002
    })
    @DeleteMapping("/{fileName}")
    ApiResponseDTO<Void> deleteAudio(
            @Parameter(description = "삭제할 오디오 파일명", required = true) @PathVariable String fileName,
            @Parameter(description = "Bearer AccessToken", required = true) HttpServletRequest request
    );
}
