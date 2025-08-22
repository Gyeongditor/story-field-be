package com.gyeongditor.storyfield.swagger;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Image", description = "이미지")
@RequestMapping("/images")
public interface ImageApi {

        @Operation(summary = "이미지 URL 조회", description = "S3에 업로드된 파일명을 통해 정적 URL을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "URL 조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {"status":200,"code":"FILE_200_003","message":"파일 URL 조회 성공",
             "data":"https://s3-....amazonaws.com/bucket/xxx.png"}"""))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {"status":401,"code":"AUTH_401_012","message":"유효하지 않은 인증 토큰입니다.","data":null}"""))),
            @ApiResponse(responseCode = "500", description = "파일 URL 조회 실패",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {"status":500,"code":"FILE_500_003","message":"파일 URL 조회 실패","data":null}""")))
    })
    @GetMapping("/{fileName}")
    ApiResponseDTO<String> getImageUrl(
            @Parameter(description = "S3에 저장된 파일명", required = true) @PathVariable String fileName,
            @Parameter(description = "Bearer AccessToken", required = true) HttpServletRequest request
    );

    @Operation(summary = "이미지 삭제", description = "S3에 업로드된 파일명을 기반으로 이미지를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {"status":204,"code":"FILE_204_001","message":"파일 삭제 성공","data":null}"""))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {"status":401,"code":"AUTH_401_012","message":"유효하지 않은 인증 토큰입니다.","data":null}"""))),
            @ApiResponse(responseCode = "500", description = "파일 삭제 실패",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {"status":500,"code":"FILE_500_004","message":"파일 삭제 실패","data":null}""")))
    })
    @DeleteMapping("/{fileName}")
    ApiResponseDTO<Void> deleteImage(
            @Parameter(description = "삭제할 파일명", required = true) @PathVariable String fileName,
            @Parameter(description = "Bearer AccessToken", required = true) HttpServletRequest request
    );
}
