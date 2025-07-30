package com.gyeongditor.storyfield.Controller;

import com.gyeongditor.storyfield.dto.Story.SaveStoryDTO;
import com.gyeongditor.storyfield.dto.Story.StoryPageResponseDTO;
import com.gyeongditor.storyfield.dto.Story.StoryThumbnailResponseDTO;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.service.StoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stories")
public class StoryController {

    private final StoryService storyService;

    @Operation(summary = "스토리 저장", description = "사용자가 스토리를 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "스토리 생성 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PostMapping("/{userId}")
    public ApiResponseDTO<String> saveStory(@PathVariable UUID userId, @Valid @RequestBody SaveStoryDTO dto) {
        return storyService.saveStory(userId, dto);
    }

    @Operation(summary = "스토리 페이지 조회", description = "스토리 ID에 해당하는 페이지 전체 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스토리 페이지 조회 성공"),
            @ApiResponse(responseCode = "404", description = "스토리를 찾을 수 없음")
    })
    @GetMapping("/{storyId}/pages")
    public ApiResponseDTO<List<StoryPageResponseDTO>> getStoryPages(@PathVariable UUID storyId) {
        return storyService.getStoryPages(storyId);
    }

    @Operation(summary = "메인 페이지 스토리 조회", description = "메인 페이지에서 스토리 목록(썸네일)을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "스토리 목록 조회 성공")
    @GetMapping("/main-thumbnails")
    public ApiResponseDTO<List<StoryThumbnailResponseDTO>> getMainPageStories(@RequestParam(defaultValue = "0") int page) {
        return storyService.getMainPageStories(page);
    }

    @Operation(summary = "스토리 삭제", description = "사용자가 본인 스토리를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "스토리 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "스토리 또는 사용자를 찾을 수 없음")
    })
    @DeleteMapping("/{userId}/{storyId}")
    public ApiResponseDTO<Void> deleteStory(@PathVariable UUID userId, @PathVariable UUID storyId) {
        return storyService.deleteStory(userId, storyId);
    }
}
