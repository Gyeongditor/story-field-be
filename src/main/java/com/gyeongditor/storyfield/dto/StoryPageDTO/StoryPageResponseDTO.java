package com.gyeongditor.storyfield.dto.StoryPageDTO;

import lombok.Data;

@Data
public class StoryPageResponseDTO {
    private String storyPageId;
    private String storyId;
    private Integer storyPageNum;
    private String storyText;
    private String storyImageUrl;
    private String storyAudioUrl;
}
