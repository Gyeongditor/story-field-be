package com.gyeongditor.storyfield.Controller;

import com.gyeongditor.storyfield.dto.Story.SaveStoryDTO;
import com.gyeongditor.storyfield.dto.Story.StoryPageResponseDTO;
import com.gyeongditor.storyfield.dto.Story.StoryThumbnailResponseDTO;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.service.StoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Tag(name = "Story", description = "동화")
@RestController
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;

    @Operation(summary = "스토리 페이지 저장", description = "FastAPI가 생성한 스토리 페이지 데이터를 저장합니다.",
            parameters = {
                    @Parameter(name = "Authorization", description = "Bearer {accessToken}", required = true)
            })
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "스토리 페이지 저장 성공"),
            @ApiResponse(responseCode = "404", description = "스토리를 찾을 수 없음")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponseDTO<String> saveStory(
            @RequestHeader("Authorization") String accessToken,
            @RequestPart("story") SaveStoryDTO saveStoryDTO,
            @RequestPart("thumbnail") MultipartFile thumbnail,
            @RequestPart("page_images") List<MultipartFile> pageImages
    ) throws IOException {
        return storyService.saveStoryFromFastApi(
                accessToken.replace("Bearer ", ""), saveStoryDTO, thumbnail, pageImages);
    }

    @Operation(summary = "스토리 페이지 조회", description = "스토리 ID에 해당하는 전체 페이지 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스토리 페이지 조회 성공"),
            @ApiResponse(responseCode = "404", description = "스토리를 찾을 수 없음")
    })
    @GetMapping("/api/stories/{storyId}")
    public ApiResponseDTO<List<StoryPageResponseDTO>> getStoryPages(@RequestHeader("Authorization") String accessToken, @PathVariable UUID storyId) {
        return storyService.getStoryPages(storyId, accessToken);
    }

    @Operation(summary = "메인 페이지 스토리 목록 조회", description = "최신 스토리 썸네일 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스토리 목록 조회 성공")
    })
    @GetMapping("/stories/main")
    public ApiResponseDTO<List<StoryThumbnailResponseDTO>> getMainPageStories(
            @RequestHeader("Authorization") String accessToken,
            @RequestParam(defaultValue = "0") int page) {
        return storyService.getMainPageStories(page, accessToken);
    }

    @Operation(summary = "스토리 삭제", description = "accessToken 기반으로 본인 스토리를 삭제합니다.",
            parameters = {
                    @Parameter(name = "Authorization", description = "Bearer {accessToken}", required = true)
            })
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "스토리 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "스토리 또는 사용자를 찾을 수 없음")
    })
    @DeleteMapping("/api/stories/{storyId}")
    public ApiResponseDTO<Void> deleteStory(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID storyId) {
        return storyService.deleteStory(accessToken.replace("Bearer ", ""), storyId);
    }
}
