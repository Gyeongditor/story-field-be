package com.gyeongditor.storyfield.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_story_page_pageNumber", columnNames = {"story_id", "pageNumber"})
        }
)
public class StoryPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int pageNumber;

    @Column(columnDefinition = "TEXT")
    private String content;

    // presignedUrl 대신 S3에 업로드된 실제 파일 이름만 저장
    @Column(nullable = false)
    private String imageFileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id")
    private Story story;
}
