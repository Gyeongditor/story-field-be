package com.gyeongditor.storyfield.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "story_page")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryPage {

    @Id
    @Column(name = "story_page_id", length = 36)
    private String storyPageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "story_page_num")
    private Integer storyPageNum;

    @Column(name = "story_text", length = 255)
    private String storyText;

    @Column(name = "story_image_url", length = 255)
    private String storyImageUrl;

    @Column(name = "story_audio_url", length = 255)
    private String storyAudioUrl;
}
