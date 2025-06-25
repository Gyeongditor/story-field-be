package com.gyeongditor.storyfield.dto.Story;

import lombok.Data;

import java.util.List;

@Data
public class SaveStoryDTO {
    private String storyTitle;
    private List<StoryPageDTO> pages;
}
