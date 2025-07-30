package com.gyeongditor.storyfield.Controller;


import com.gyeongditor.storyfield.dto.Story.SaveStoryDTO;
import com.gyeongditor.storyfield.dto.Story.StoryPageResponseDTO;
import com.gyeongditor.storyfield.dto.Story.StoryThumbnailResponseDTO;
import com.gyeongditor.storyfield.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/story")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;

    @PostMapping("/{userId}")
    public String saveStory(@PathVariable UUID userId, @RequestBody SaveStoryDTO dto) {
        return storyService.saveStory(userId, dto);
    }

    @GetMapping("/{storyId}/pages")
    public List<StoryPageResponseDTO> getStoryPages(@PathVariable UUID storyId) {
        return storyService.getStoryPages(storyId);
    }

    @GetMapping("/main-thumbnails")
    public List<StoryThumbnailResponseDTO> getMainThumbnails(@RequestParam(defaultValue = "0") int page) {
        return storyService.getMainPageStories(page);
    }

    @DeleteMapping("/{userId}/{storyId}")
    public void deleteStory(@PathVariable UUID userId, @PathVariable UUID storyId) {
        storyService.deleteStory(userId, storyId);
    }
}
