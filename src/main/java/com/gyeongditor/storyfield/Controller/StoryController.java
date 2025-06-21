package com.gyeongditor.storyfield.Controller;

import com.gyeongditor.storyfield.dto.StoryDTO.StoryCreateRequestDTO;
import com.gyeongditor.storyfield.dto.StoryDTO.StoryResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stories")
@Tag(name = "Story API", description = "동화 생성 및 조회 API")
public class StoryController {

    @PostMapping
    @Operation(summary = "동화 생성", description = "사용자가 새 동화를 생성.")
    public ResponseEntity<StoryResponseDTO> createStory(@RequestBody StoryCreateRequestDTO requestDTO) {
        // 생성 로직
        return ResponseEntity.ok(new StoryResponseDTO());
    }

    @GetMapping("/{storyId}")
    @Operation(summary = "동화 상세 조회", description = "특정 동화의 정보를 반환.")
    public ResponseEntity<StoryResponseDTO> getStory(@PathVariable String storyId) {
        // 조회 로직
        return ResponseEntity.ok(new StoryResponseDTO());
    }
}
