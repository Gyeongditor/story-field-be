package com.gyeongditor.storyfield.dto.UserDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import jakarta.validation.constraints.Pattern;

@Getter
@Setter
@NoArgsConstructor(force = true)  // Jackson이 필요로 함
@AllArgsConstructor // 생성자 기반 주입도 가능@Schema(description = "회원가입 요청 DTO")
public class SignUpDTO {

    @Schema(description = "회원가입 이메일 주소", example = "newuser@example.com")
    @NotBlank(message = "이메일을 입력해주세요")
    private final String email;

    @Schema(description = "회원가입 비밀번호", example = "SignUp123!")
    @NotBlank(message = "비밀번호를 입력해주세요")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$",
            message = "비밀번호는 영문 대소문자, 숫자, 특수문자를 포함하여 8~16자여야 합니다")
    private final String password;

    @Schema(description = "회원 이름", example = "홍길동")
    @NotBlank(message = "이름을 입력해주세요")
    private final String username;

    @Override
    public String toString() {
        return "SignUpRequest{" +
                "username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                ", userId='" + email + '\'' +
                '}';
    }
}
