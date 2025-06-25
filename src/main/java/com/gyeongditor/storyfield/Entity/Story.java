package com.gyeongditor.storyfield.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "story")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Story {

    @Id
    @Column(name = "story_id", length = 36)
    private String storyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "story_title", length = 255)
    private String storyTitle;

    @Column(name = "story_created_at")
    private LocalDateTime storyCreatedAt;

    @Column(name = "story_updated_at")
    private LocalDateTime storyUpdatedAt;
}

