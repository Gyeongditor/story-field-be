package com.gyeongditor.storyfield.dto.StoryPageDTO;

import lombok.Data;

@Data
public class StoryPageCreateRequestDTO {
    private String storyId;
    private String userId;
    private Integer storyPageNum;
    private String storyText;
    private String storyImageUrl;
    private String storyAudioUrl;
}
