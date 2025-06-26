package com.gyeongditor.storyfield.Controller;


import com.gyeongditor.storyfield.dto.Story.SaveStoryDTO;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/story")
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
    public ResponseEntity<String> saveStory(
            @Parameter(description = "사용자 UUID", required = true)
            @PathVariable UUID userId,

            @org.springframework.web.bind.annotation.RequestBody SaveStoryDTO dto
    ) {
        String storyId = storyService.saveStory(userId, dto);
        return ResponseEntity.ok(storyId);
    }
}
