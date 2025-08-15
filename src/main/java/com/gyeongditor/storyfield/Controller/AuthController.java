package com.gyeongditor.storyfield.Controller;

import com.gyeongditor.storyfield.dto.UserDTO.LoginDTO;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;   // Swagger 전용
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Auth", description = "인증")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 로그인
     */
    @Operation(summary = "로그인", description = "이메일과 비밀번호를 통해 로그인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (아이디 또는 비밀번호 불일치)"),
            @ApiResponse(responseCode = "403", description = "계정이 활성화되지 않음"),
            @ApiResponse(responseCode = "423", description = "계정 잠금"),
            @ApiResponse(responseCode = "404", description = "계정 없음")
    })
    @PostMapping("/login")
    public ApiResponseDTO<Map<String, String>> login(
            @Valid @RequestBody LoginDTO loginDTO,
            HttpServletResponse response
    ) {
        return authService.login(loginDTO.getEmail(), loginDTO.getPassword(), response);
    }

    /**
     * 로그아웃
     */
    @Operation(summary = "로그아웃", description = "Refresh Token을 통해 로그아웃합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @ApiResponse(responseCode = "500", description = "로그아웃 처리 중 서버 오류")
    })
    @DeleteMapping("/logout")
    public ApiResponseDTO<String> logout(@RequestHeader(name = "Refresh-Token") String refreshToken) {
        return authService.logout(refreshToken);
    }
}
