package com.gyeongditor.storyfield.dto.UserDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "회원가입 요청 DTO")
public class UserSignupRequestDTO {

    @Schema(description = "로그인 ID", example = "gyeongdi123")
    private String loginId;

    @Schema(description = "비밀번호", example = "password123!")
    private String userPw;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "나이", example = "25")
    private Integer age;

    @Schema(description = "성별", example = "M 또는 F")
    private String sex;

    @Schema(description = "이메일 주소", example = "gyeongdi@example.com")
    private String userEmail;
}
