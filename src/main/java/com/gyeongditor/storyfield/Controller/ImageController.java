package com.gyeongditor.storyfield.Controller;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageController {

    private final S3Service s3Service;

    @Operation(
            summary = "Presigned URL 생성",
            description = """
        클라이언트가 AWS S3에 직접 파일을 업로드할 수 있도록
        임시로 유효한 Presigned URL을 생성하여 반환합니다.
        
        이 URL은 10분간 유효하며, 해당 URL로 `PUT` 요청을 보내면
        서버를 거치지 않고 S3에 직접 파일 업로드가 가능합니다.
        
        🔹 사용 시점:
        - 이미지, 파일 등을 사용자가 직접 업로드해야 할 때
        - 서버가 파일 내용을 직접 저장하지 않고 S3로 위임할 때
        - 보안상 짧은 시간만 접근 허용해야 할 때
        """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Presigned URL 생성 성공"),
            @ApiResponse(responseCode = "500", description = "Presigned URL 생성 실패")
    })
    @GetMapping("/image-url")
    public ApiResponseDTO<String> getPresignedUrl(
            @Parameter(description = "S3에 저장될 파일명 (확장자 포함)", required = true)
            @RequestParam String fileName
    ) {
        return s3Service.generatePresignedUrl(fileName);
    }


    @Operation(
            summary = "이미지 업로드",
            description = "Multipart 형식으로 이미지를 업로드하고, 업로드된 이미지의 S3 URL을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업로드 성공"),
            @ApiResponse(responseCode = "500", description = "업로드 실패")
    })
    @PostMapping
    public ApiResponseDTO<String> uploadImage(
            @Parameter(description = "업로드할 이미지 파일", required = true)
            @RequestParam("file") MultipartFile file
    ) {
        return s3Service.uploadFile(file);
    }

    @Operation(
            summary = "이미지 URL 조회",
            description = "S3에 업로드된 파일명을 통해 정적 이미지 URL을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "URL 조회 성공"),
            @ApiResponse(responseCode = "500", description = "URL 조회 실패")
    })
    @GetMapping("/{fileName}")
    public ApiResponseDTO<String> getImageUrl(
            @Parameter(description = "S3에 저장된 파일명", required = true)
            @PathVariable String fileName
    ) {
        return s3Service.getFileUrlResponse(fileName);
    }

    @Operation(
            summary = "이미지 삭제",
            description = "S3에 업로드된 파일명을 기반으로 이미지를 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "500", description = "삭제 실패")
    })
    @DeleteMapping("/{fileName}")
    public ApiResponseDTO<Void> deleteImage(
            @Parameter(description = "삭제할 파일명", required = true)
            @PathVariable String fileName
    ) {
        return s3Service.deleteFile(fileName);
    }
}
