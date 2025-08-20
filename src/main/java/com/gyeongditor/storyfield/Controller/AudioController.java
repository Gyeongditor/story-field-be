package com.gyeongditor.storyfield.Controller;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Audio", description = "오디오 파일 업로드 및 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/audio")
public class AudioController {

    private final S3Service s3Service;

    @SneakyThrows
    @Operation(
            summary = "오디오 업로드",
            description = "Multipart 형식으로 오디오 파일을 업로드하고, 업로드된 오디오의 S3 URL을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "업로드 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {
              "status": 200,
              "code": "FILE_200_001",
              "message": "파일 업로드 성공",
              "data": "https://s3-....amazonaws.com/bucket/audio/abc.mp3"
            }
            """))),
            @ApiResponse(
                    responseCode = "401",
                    description = "AccessToken 누락 또는 유효하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {
              "status": 401,
              "code": "AUTH_401_012",
              "message": "유효하지 않은 인증 토큰입니다.",
              "data": null
            }
            """))),
            @ApiResponse(
                    responseCode = "413",
                    description = "허용된 파일 크기 초과",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {
              "status": 413,
              "code": "FILE_413_002",
              "message": "허용된 파일 크기를 초과했습니다.",
              "data": null
            }
            """))),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 파일 형식",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {
              "status": 400,
              "code": "FILE_400_002",
              "message": "허용되지 않은 파일 형식입니다.",
              "data": null
            }
            """))),
            @ApiResponse(
                    responseCode = "500",
                    description = "업로드 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {
              "status": 500,
              "code": "FILE_500_001",
              "message": "파일 업로드 중 오류 발생",
              "data": null
            }
            """)))})
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponseDTO<String> uploadAudio(
            @Parameter(
                    description = "업로드할 오디오 파일 (mp3, wav, m4a 등)",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Bearer AccessToken", required = true)
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String accessToken = authorizationHeader.replace("Bearer ", "");
        return s3Service.uploadAudioFile(file, accessToken);
    }

    @Operation(
            summary = "오디오 URL 조회",
            description = "S3에 업로드된 오디오 파일명을 통해 정적 URL을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "URL 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {
              "status": 200,
              "code": "FILE_200_003",
              "message": "파일 URL 조회 성공",
              "data": "https://s3-....amazonaws.com/bucket/audio/xxx.wav"
            }
            """))),
            @ApiResponse(
                    responseCode = "401",
                    description = "AccessToken 누락 또는 유효하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {
              "status": 401,
              "code": "AUTH_401_012",
              "message": "유효하지 않은 인증 토큰입니다.",
              "data": null
            }
            """))),
            @ApiResponse(
                    responseCode = "500",
                    description = "파일 URL 조회 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {
              "status": 500,
              "code": "FILE_500_003",
              "message": "파일 URL 조회 실패",
              "data": null
            }
            """)))})
    @GetMapping("/{fileName}")
    public ApiResponseDTO<String> getAudioUrl(
            @Parameter(description = "S3에 저장된 오디오 파일명", required = true)
            @PathVariable String fileName,
            @Parameter(description = "Bearer AccessToken", required = true)
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String accessToken = authorizationHeader.replace("Bearer ", "");
        return s3Service.getFileUrlResponse(fileName, accessToken);
    }

    @Operation(
            summary = "Presigned URL 생성",
            description = """
            클라이언트가 AWS S3에 직접 파일을 업로드할 수 있도록
            10분간 유효한 Presigned URL을 생성하여 반환합니다.

            ✅ AccessToken이 필요하며, 인가된 사용자만 URL을 발급받을 수 있습니다.
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Presigned URL 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {
              "status": 200,
              "code": "FILE_200_002",
              "message": "Presigned URL 생성 성공",
              "data": "https://s3-....amazonaws.com/bucket/key?X-Amz-Expires=600&..."
            }
            """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "AccessToken 누락 또는 유효하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {
              "status": 401,
              "code": "AUTH_401_012",
              "message": "유효하지 않은 인증 토큰입니다.",
              "data": null
            }
            """)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Presigned URL 생성 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {
              "status": 500,
              "code": "FILE_500_002",
              "message": "Presigned URL 생성 실패",
              "data": null
            }
            """)
                    )
            )
    })
    @GetMapping("/presign")
    public ApiResponseDTO<String> getPresignedUrl(
            @Parameter(description = "S3에 저장될 음성파일명 (확장자 포함)", required = true)
            @RequestParam String fileName,

            @Parameter(description = "Bearer AccessToken", required = true)
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String accessToken = authorizationHeader.replace("Bearer ", "");
        return s3Service.generatePresignedUrl(fileName, accessToken);
    }
    
    @Operation(
            summary = "오디오 삭제",
            description = "S3에 업로드된 오디오 파일명을 기반으로 오디오 파일을 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {
              "status": 204,
              "code": "FILE_204_001",
              "message": "파일 삭제 성공",
              "data": null
            }
            """))),
            @ApiResponse(
                    responseCode = "401",
                    description = "AccessToken 누락 또는 유효하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {
              "status": 401,
              "code": "AUTH_401_012",
              "message": "유효하지 않은 인증 토큰입니다.",
              "data": null
            }
            """))),
            @ApiResponse(
                    responseCode = "500",
                    description = "파일 삭제 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {
              "status": 500,
              "code": "FILE_500_004",
              "message": "파일 삭제 실패",
              "data": null
            }
            """)))})
    @DeleteMapping("/{fileName}")
    public ApiResponseDTO<Void> deleteAudio(
            @Parameter(description = "삭제할 오디오 파일명", required = true)
            @PathVariable String fileName,
            @Parameter(description = "Bearer AccessToken", required = true)
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String accessToken = authorizationHeader.replace("Bearer ", "");
        return s3Service.deleteFile(fileName, accessToken);
    }
}
