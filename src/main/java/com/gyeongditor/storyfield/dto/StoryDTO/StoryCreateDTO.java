package com.gyeongditor.storyfield.dto.StoryDTO;

import com.gyeongditor.storyfield.Entity.Story;
import com.gyeongditor.storyfield.Entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StoryCreateDTO {
    private String userId;
    private String storyTitle;
    private String storyContent;


    public Story toEntity(User user) {
        return Story.builder()
                .storyId(java.util.UUID.randomUUID().toString())
                .user(user)
                .storyTitle(storyTitle)
                .storyCreatedAt(LocalDateTime.now())
                .storyUpdatedAt(LocalDateTime.now())
                .build();
    }
}
