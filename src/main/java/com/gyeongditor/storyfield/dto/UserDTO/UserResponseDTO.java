package com.gyeongditor.storyfield.dto.UserDTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponseDTO {
    private String userId;
    private String name;
    private Integer age;
    private String sex;
    private String userEmail;
    private LocalDateTime createdAt;
}
