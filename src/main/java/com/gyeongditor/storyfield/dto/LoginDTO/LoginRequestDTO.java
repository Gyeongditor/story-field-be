package com.gyeongditor.storyfield.dto.LoginDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "로그인 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    @Schema(description = "로그인 아이디", example = "user123", required = true)
    private String loginId;

    @Schema(description = "비밀번호", example = "password", required = true)
    private String userPw;
}
