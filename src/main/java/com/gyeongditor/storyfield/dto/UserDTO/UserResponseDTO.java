package com.gyeongditor.storyfield.dto.UserDTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserResponseDTO {
    private String userId;
    private String name;
    private Integer age;
    private String sex;
    private String userEmail;
    private LocalDateTime createdAt;
}
