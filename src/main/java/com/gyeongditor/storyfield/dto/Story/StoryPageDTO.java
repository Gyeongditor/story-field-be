package com.gyeongditor.storyfield.dto.Story;

import lombok.Data;

@Data
public class StoryPageDTO {
    private int pageNumber;
    private String content;
    private String imageUrl;
}