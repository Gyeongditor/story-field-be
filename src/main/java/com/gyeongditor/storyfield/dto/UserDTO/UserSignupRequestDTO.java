package com.gyeongditor.storyfield.dto.UserDTO;

import lombok.Data;

@Data
public class UserSignupRequestDTO {
    private String loginId;
    private String userPw;
    private String name;
    private Integer age;
    private String sex;
    private String userEmail;
}
