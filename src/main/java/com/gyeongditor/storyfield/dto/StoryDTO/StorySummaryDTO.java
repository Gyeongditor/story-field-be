package com.gyeongditor.storyfield.dto.StoryDTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StorySummaryDTO {
    private String storyId;
    private String storyTitle;
    private String imageUrl;
}
