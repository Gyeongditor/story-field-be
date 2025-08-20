package com.gyeongditor.storyfield.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audio")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Audio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;      // 원본 파일명

    @Column(nullable = false, unique = true)
    private String fileUrl;       // S3 업로드 URL
    private Long fileSize;        // 파일 크기
    private String contentType;   // MIME 타입

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // User와 연관관계 (N:1)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false) // FK (user.user_id)
    private User user;
}
