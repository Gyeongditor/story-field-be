package com.gyeongditor.storyfield.dto.UserDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "유저 응답 DTO")
public class UserResponseDTO {

    @Schema(description = "사용자 고유 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;
    @Schema(description = "사용자 이메일", example = "user@example.com")
    private String email;
    @Schema(description = "사용자 이름", example = "홍길동")
    private String username;

    public UserResponseDTO(String email, String username) {
        this.email = email;
        this.username = username;
    }
}
