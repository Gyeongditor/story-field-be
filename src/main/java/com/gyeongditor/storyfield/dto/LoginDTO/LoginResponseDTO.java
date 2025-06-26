package com.gyeongditor.storyfield.dto.LoginDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "로그인 응답 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    @Schema(description = "JWT 액세스 토큰")
    private String accessToken;
}
