package com.gyeongditor.storyfield.dto.StoryDTO;

import com.gyeongditor.storyfield.Entity.StoryPage;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StoryPageResponseDTO {
    private Integer pageNum;
    private String text;
    private String imageUrl;
    private String audioUrl;

    public static StoryPageResponseDTO fromEntity(StoryPage page) {
        StoryPageResponseDTO dto = new StoryPageResponseDTO();
        dto.setPageNum(page.getStoryPageNum());
        dto.setText(page.getStoryText());
        dto.setImageUrl(page.getStoryImageUrl());
        dto.setAudioUrl(page.getStoryAudioUrl());
        return dto;
    }
}