package com.gyeongditor.storyfield.dto.StoryDTO;

import com.gyeongditor.storyfield.Entity.StoryPage;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
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