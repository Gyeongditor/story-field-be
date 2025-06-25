package com.gyeongditor.storyfield.controller;

import com.gyeongditor.storyfield.Entity.User;
import com.gyeongditor.storyfield.dto.StoryDTO.StoryCreateDTO;
import com.gyeongditor.storyfield.dto.StoryDTO.StoryPageResponseDTO;
import com.gyeongditor.storyfield.dto.StoryDTO.StorySummaryDTO;
import com.gyeongditor.storyfield.repository.UserRepository;
import com.gyeongditor.storyfield.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stories")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;
    private final UserRepository userRepository;

    // 1. 동화 생성
    @PostMapping
    public ResponseEntity<String> createStory(@RequestBody StoryCreateDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String storyId = storyService.createStory(request, user);
        return ResponseEntity.ok(storyId);
    }

    // 2. 전체 동화 목록 조회
    @GetMapping
    public ResponseEntity<List<StorySummaryDTO>> getAllStories() {
        List<StorySummaryDTO> stories = storyService.readAllStory();
        return ResponseEntity.ok(stories);
    }

    // 3. 특정 스토리 페이지 전체 조회
    @GetMapping("/{storyId}/pages")
    public ResponseEntity<List<StoryPageResponseDTO>> getStoryPages(@PathVariable String storyId) {
        List<StoryPageResponseDTO> pages = storyService.getPagesByStoryId(storyId);
        return ResponseEntity.ok(pages);
    }

    // 4. 동화 삭제
    @DeleteMapping("/{storyId}")
    public ResponseEntity<String> deleteStory(@PathVariable String storyId) {
        String deletedId = storyService.deleteStory(storyId);
        return ResponseEntity.ok(deletedId);
    }
}
