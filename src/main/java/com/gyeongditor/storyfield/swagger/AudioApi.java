package com.gyeongditor.storyfield.swagger;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Audio", description = "오디오 파일 업로드 및 관리")
@RequestMapping("/api/audio")
public interface AudioApi {

    @Operation(summary = "오디오 업로드", description = "Multipart 형식으로 오디오 파일을 업로드하고, 업로드된 오디오의 S3 URL을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업로드 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {"status":200,"code":"AUDIO_200_001","message":"오디오 파일 업로드 성공",
             "data":"https://s3-....amazonaws.com/bucket/audio/abc.mp3"}"""))),
            @ApiResponse(responseCode = "400", description = "잘못된 오디오 파일",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {"status":400,"code":"AUDIO_400_002","message":"허용되지 않은 오디오 파일 형식입니다.","data":null}"""))),
            @ApiResponse(responseCode = "413", description = "허용된 파일 크기 초과",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {"status":413,"code":"AUDIO_413_001","message":"허용된 오디오 파일 크기를 초과했습니다.","data":null}"""))),
            @ApiResponse(responseCode = "500", description = "업로드 실패",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {"status":500,"code":"AUDIO_500_001","message":"오디오 파일 업로드 중 오류가 발생했습니다.","data":null}""")))
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

    @Operation(summary = "오디오 URL 조회", description = "S3에 업로드된 오디오 파일명을 통해 정적 URL을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "URL 조회 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {"status":200,"code":"AUDIO_200_003","message":"오디오 파일 URL 조회 성공",
             "data":"https://s3-....amazonaws.com/bucket/audio/xxx.wav"}"""))),
            @ApiResponse(responseCode = "404", description = "오디오 파일 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {"status":404,"code":"AUDIO_404_001","message":"오디오 파일을 찾을 수 없습니다.","data":null}"""))),
            @ApiResponse(responseCode = "500", description = "파일 URL 조회 실패",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {"status":500,"code":"AUDIO_500_003","message":"오디오 파일 URL 조회 실패","data":null}""")))
    })
    @GetMapping("/{fileName}")
    ApiResponseDTO<String> getAudioUrl(
            @Parameter(description = "S3에 저장된 오디오 파일명", required = true) @PathVariable String fileName,
            @Parameter(description = "Bearer AccessToken", required = true) HttpServletRequest request
    );

    @Operation(summary = "다운로드용 Presigned URL 발급", description = "private 버킷에서 오디오 파일을 GET으로 내려받기 위한 Presigned URL을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "다운로드 Presigned URL 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {"status":200,"code":"AUDIO_200_003","message":"오디오 파일 URL 조회 성공",
             "data":"https://s3-....amazonaws.com/bucket/audio/uuid_name.m4a?X-Amz-Expires=600&..."}"""))),
            @ApiResponse(responseCode = "404", description = "오디오 파일 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {"status":404,"code":"AUDIO_404_001","message":"오디오 파일을 찾을 수 없습니다.","data":null}""")))
    })
    @GetMapping("/presign/download")
    ApiResponseDTO<String> getDownloadPresignedUrl(
            @Parameter(description = "S3 오디오 키 또는 파일명", required = true) @RequestParam String keyOrFileName,
            @Parameter(description = "Bearer AccessToken", required = true) HttpServletRequest request
    );

    @Operation(summary = "오디오 삭제", description = "S3에 업로드된 오디오 파일명을 기반으로 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {"status":204,"code":"AUDIO_204_001","message":"오디오 파일 삭제 성공","data":null}"""))),
            @ApiResponse(responseCode = "404", description = "오디오 파일 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {"status":404,"code":"AUDIO_404_001","message":"오디오 파일을 찾을 수 없습니다.","data":null}"""))),
            @ApiResponse(responseCode = "500", description = "삭제 실패",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {"status":500,"code":"AUDIO_500_002","message":"오디오 파일 삭제 중 오류가 발생했습니다.","data":null}""")))
    })
    @DeleteMapping("/{fileName}")
    ApiResponseDTO<Void> deleteAudio(
            @Parameter(description = "삭제할 오디오 파일명", required = true) @PathVariable String fileName,
            @Parameter(description = "Bearer AccessToken", required = true) HttpServletRequest request
    );
}
