package com.gyeongditor.storyfield.Controller;


import com.gyeongditor.storyfield.dto.Story.SaveStoryDTO;
import com.gyeongditor.storyfield.dto.Story.StoryPageResponseDTO;
import com.gyeongditor.storyfield.dto.Story.StoryThumbnailResponseDTO;
import com.gyeongditor.storyfield.service.StoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/story")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;

    @PostMapping("/{userId}")
    @Operation(
            summary = "스토리 저장",
            description = "사용자의 동화와 페이지 정보를 저장합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스토리 저장 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    public ResponseEntity<String> saveStory(@Parameter(description = "사용자 UUID", required = true)
            @PathVariable UUID userId,
            @org.springframework.web.bind.annotation.RequestBody SaveStoryDTO dto) {
        String storyId = storyService.saveStory(userId, dto);
        return ResponseEntity.ok(storyId);
    }

    @GetMapping("/{storyId}/pages")
    @Operation(summary = "스토리 페이지 조회", description = "스토리를 페이지별로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "스토리 없음")
    })
    public ResponseEntity<List<StoryPageResponseDTO>> getStoryPages(@Parameter(description = "스토리 UUID", required = true) @PathVariable UUID storyId) {
        List<StoryPageResponseDTO> pages = storyService.getStoryPages(storyId);
        return ResponseEntity.ok(pages);
    }

    @GetMapping("/main-thumbnails")
    @Operation(summary = "메인화면 스토리 썸네일 목록 (페이징)", description = "스토리 제목과 썸네일을 페이지별로 가져옵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<List<StoryThumbnailResponseDTO>> getMainThumbnails(@RequestParam(defaultValue = "0") int page) {
        List<StoryThumbnailResponseDTO> thumbnails = storyService.getMainPageStories(page);
        return ResponseEntity.ok(thumbnails);
    }

    @DeleteMapping("/{userId}/{storyId}")
    @Operation(
            summary = "스토리 삭제",
            description = "스토리 ID를 이용하여 해당 동화를 삭제합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "스토리 없음")
    })
    public ResponseEntity<Void> deleteStory(@Parameter(description = "스토리 ID", required = true) @PathVariable UUID userId, @PathVariable UUID storyId) {
        storyService.deleteStory(userId, storyId);
        return ResponseEntity.ok().build();
    }
}
