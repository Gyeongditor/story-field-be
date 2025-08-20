package com.gyeongditor.storyfield.Controller;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.service.S3Service;
import io.jsonwebtoken.io.IOException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestPart;

import java.util.List;

@Tag(name = "Image", description = "이미지")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/images")
public class ImageController {

    private final S3Service s3Service;

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
            @Parameter(description = "S3에 저장될 파일명 (확장자 포함)", required = true)
            @RequestParam String fileName,

            @Parameter(description = "Bearer AccessToken", required = true)
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String accessToken = authorizationHeader.replace("Bearer ", "");
        return s3Service.generatePresignedUrl(fileName, accessToken);
    }

    @SneakyThrows
    @Operation(
            summary = "이미지 업로드",
            description = "Multipart 형식으로 이미지를 업로드하고, 업로드된 이미지의 S3 URL을 반환합니다."
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
              "data": "https://s3-....amazonaws.com/bucket/abc.png"
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
                    responseCode = "413",
                    description = "허용된 요청 크기 초과",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {
              "status": 413,
              "code": "REQ_413_001",
              "message": "허용된 요청 크기 초과",
              "data": null
            }
            """)
                    )
            ),
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
            """)
                    )
            )
    })
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponseDTO<List<String>> uploadImage(
            @Parameter(
                    description = "업로드할 이미지 파일들",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "Bearer AccessToken", required = true)
            @RequestHeader("Authorization") String authorizationHeader
    ) throws IOException {
        String accessToken = authorizationHeader.replace("Bearer ", "");
        return s3Service.uploadFiles(files, accessToken);
    }
    @SneakyThrows
    @Operation(
            summary = "이미지 업로드",
            description = "Multipart 형식으로 이미지를 업로드하고, 업로드된 이미지의 S3 URL을 반환합니다."
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
              "data": "https://s3-....amazonaws.com/bucket/abc.png"
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
                    responseCode = "413",
                    description = "허용된 요청 크기 초과",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {
              "status": 413,
              "code": "REQ_413_001",
              "message": "허용된 요청 크기 초과",
              "data": null
            }
            """)
                    )
            ),
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
            """)
                    )
            )
    })
    @PostMapping(path = "/thumbnail/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponseDTO<String> uploadThumbnailFile(
            @Parameter(description = "업로드할 썸네일 파일", required = true)
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "JWT 액세스 토큰", required = true, example = "Bearer eyJhbGciOiJIUzI1NiJ9...")
            @RequestHeader("Authorization") String authorizationHeader
    ) throws IOException {

        // Authorization 헤더에서 토큰 추출 (Bearer 접두사 제거)
        String accessToken = authorizationHeader.replace("Bearer ", "");

        // 서비스 호출
        return  s3Service.uploadThumbnailFile(file, accessToken);
    }

    @Operation(
            summary = "이미지 URL 조회",
            description = "S3에 업로드된 파일명을 통해 정적 이미지 URL을 반환합니다."
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
              "data": "https://s3-....amazonaws.com/bucket/xxx.png"
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
            """)))
    })
    @GetMapping("/{fileName}")
    public ApiResponseDTO<String> getImageUrl(
            @Parameter(description = "S3에 저장된 파일명", required = true)
            @PathVariable String fileName,

            @Parameter(description = "Bearer AccessToken", required = true)
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String accessToken = authorizationHeader.replace("Bearer ", "");
        return s3Service.getFileUrlResponse(fileName, accessToken);
    }

    @Operation(
            summary = "이미지 삭제",
            description = "S3에 업로드된 파일명을 기반으로 이미지를 삭제합니다."
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
            """)
                    )
            )
    })
    @DeleteMapping("/{fileName}")
    public ApiResponseDTO<Void> deleteImage(
            @Parameter(description = "삭제할 파일명", required = true)
            @PathVariable String fileName,

            @Parameter(description = "Bearer AccessToken", required = true)
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String accessToken = authorizationHeader.replace("Bearer ", "");
        return s3Service.deleteFile(fileName, accessToken);
    }
}

