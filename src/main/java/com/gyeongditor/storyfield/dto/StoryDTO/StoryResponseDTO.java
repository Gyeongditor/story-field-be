package com.gyeongditor.storyfield.dto.StoryDTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StoryResponseDTO {
    private String storyId;
    private String storyTitle;
    private String userId;
    private LocalDateTime storyCreatedAt;
    private LocalDateTime storyUpdatedAt;
}
