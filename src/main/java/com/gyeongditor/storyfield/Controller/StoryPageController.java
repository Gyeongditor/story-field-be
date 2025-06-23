package com.gyeongditor.storyfield.Controller;

import com.gyeongditor.storyfield.dto.StoryPageDTO.StoryPageCreateRequestDTO;
import com.gyeongditor.storyfield.dto.StoryPageDTO.StoryPageResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stories/{storyId}/pages")
@Tag(name = "StoryPage API", description = "동화 컷(페이지) API")
public class StoryPageController {

    @PostMapping
    @Operation(summary = "동화 컷 생성", description = "동화에 새로운 컷을 추가.")
    public ResponseEntity<StoryPageResponseDTO> createPage(
            @PathVariable String storyId,
            @RequestBody StoryPageCreateRequestDTO requestDTO
    ) {
        // 컷 추가 로직
        return ResponseEntity.ok(new StoryPageResponseDTO());
    }

    @GetMapping
    @Operation(summary = "동화 컷 목록 조회", description = "동화의 모든 컷을 반환.")
    public ResponseEntity<List<StoryPageResponseDTO>> getPages(@PathVariable String storyId) {
        // 조회 로직
        return ResponseEntity.ok(List.of());
    }
}
