package com.gyeongditor.storyfield.dto.UserDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "회원 정보 수정 DTO")
public class UpdateUserDTO {

    @Schema(description = "사용자 이메일 (변경 시 사용 가능)", example = "newuser@example.com")
    private String email;

    @Schema(description = "변경할 비밀번호 (선택사항)",
            example = "NewPassword123!",
            nullable = true)
    private String password;

    @Schema(description = "변경할 사용자 이름", example = "길동이")
    @NotBlank(message = "이름을 입력해주세요")
    private String username;
}
