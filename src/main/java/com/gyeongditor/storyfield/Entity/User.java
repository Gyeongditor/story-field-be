package com.gyeongditor.storyfield.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "user_id", length = 36)
    private UUID userId;

    @Column(name = "login_id", length = 36, nullable = false, unique = true)
    private String loginId;

    @Column(name = "user_pw", length = 60, nullable = false)
    private String userPw;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    private Integer age;

    @Column(name = "sex", length = 10)
    private String sex; // 추후 Enum 처리 가능

    @Column(name = "user_email", length = 255)
    private String userEmail;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
