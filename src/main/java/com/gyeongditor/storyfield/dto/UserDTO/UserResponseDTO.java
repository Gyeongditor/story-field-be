package com.gyeongditor.storyfield.dto.UserDTO;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    private UUID id;
    private String email;
    private String userName;

    public UserResponseDTO(String email, String userName) {
        this.email = email;
        this.userName = userName;
    }
}
