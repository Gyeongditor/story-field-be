package com.gyeongditor.storyfield.dto.UserDTO;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    private UUID userId;
    private String email;
    private String username;

    public UserResponseDTO(String email, String username) {
        this.email = email;
        this.username = username;
    }
}
