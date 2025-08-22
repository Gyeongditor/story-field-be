package com.gyeongditor.storyfield.swagger.api;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.dto.Story.StoryPageResponseDTO;
import com.gyeongditor.storyfield.dto.Story.StoryThumbnailResponseDTO;
import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.response.SuccessCode;
import com.gyeongditor.storyfield.swagger.config.ApiErrorResponse;
import com.gyeongditor.storyfield.swagger.config.ApiSuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RequestMapping
public interface StoryApi {

    @Operation(
            summary = "스토리 페이지 저장",
            description = "FastAPI가 생성한 스토리 페이지 데이터와 파일들을 저장합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiSuccessResponse(
            SuccessCode.STORY_201_001
    )
    @ApiErrorResponse({
            ErrorCode.STORY_400_001, // 요청 데이터 잘못됨
            ErrorCode.FILE_400_001,  // 파일 비어 있음
            ErrorCode.FILE_400_002,  // 파일 형식 오류
            ErrorCode.FILE_413_002,  // 파일 크기 초과
            ErrorCode.SERVER_500_001 // 내부 서버 오류
    })
    @PostMapping(value = "/stories/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponseDTO<String> saveStory(
            HttpServletRequest request,
            @RequestPart("saveStoryDTO") String saveStoryDtoString,
            @RequestPart("thumbnail") MultipartFile thumbnail,
            @RequestPart("pageImages") List<MultipartFile> pageImages
    );

    @Operation(
            summary = "스토리 페이지 조회",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiSuccessResponse(
            SuccessCode.STORY_200_001
    )
    @ApiErrorResponse({
            ErrorCode.STORY_404_001, // 스토리 없음
            ErrorCode.AUTH_403_002   // 접근 권한 없음
    })
    @GetMapping("/api/stories/{storyId}")
    ApiResponseDTO<List<StoryPageResponseDTO>> getStoryPages(
            HttpServletRequest request,
            @PathVariable UUID storyId
    );

    @Operation(
            summary = "메인 페이지 스토리 목록 조회",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiSuccessResponse(
            SuccessCode.STORY_200_002
    )
    @ApiErrorResponse({
            ErrorCode.SERVER_500_001 // 내부 서버 오류
    })
    @GetMapping("/api/stories/main")
    ApiResponseDTO<List<StoryThumbnailResponseDTO>> getMainPageStories(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page
    );

    @Operation(
            summary = "스토리 삭제",
            description = "accessToken 기반으로 본인 스토리를 삭제합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiSuccessResponse(
            SuccessCode.STORY_204_001
    )
    @ApiErrorResponse({
            ErrorCode.STORY_404_001, // 스토리 없음
            ErrorCode.STORY_403_001, // 본인만 삭제 가능
            ErrorCode.AUTH_401_004   // 토큰 유효하지 않음
    })
    @DeleteMapping("/api/stories/{storyId}")
    ApiResponseDTO<Void> deleteStory(
            HttpServletRequest request,
            @PathVariable UUID storyId
    );
}
