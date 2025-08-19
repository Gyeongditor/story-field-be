package com.gyeongditor.storyfield.Controller;

import com.gyeongditor.storyfield.dto.UserDTO.LoginDTO;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;   // Swagger 전용
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Auth", description = "인증")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 로그인
     */
    @Operation(summary = "로그인", description = "이메일과 비밀번호를 통해 로그인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
      {"status":200,"code":"AUTH_200_001","message":"로그인이 성공적으로 완료되었습니다.",
       "data":{"accessToken":"eyJhbGciOiJIUzI1NiIs...","refreshToken":"eyJhbGciOiJIUzI1NiIs..."}}"""))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
      {"status":401,"code":"AUTH_401_009","message":"아이디 또는 비밀번호가 올바르지 않습니다.","data":null}"""))),
            @ApiResponse(responseCode = "403", description = "계정 미활성",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
      {"status":403,"code":"USER_403_003","message":"계정이 활성화되지 않았습니다. 이메일 인증을 완료해주세요.","data":null}"""))),
            @ApiResponse(responseCode = "404", description = "계정 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
      {"status":404,"code":"USER_404_001","message":"존재하지 않는 계정","data":null}"""))),
            @ApiResponse(responseCode = "423", description = "계정 잠금",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
      {"status":423,"code":"USER_423_002","message":"계정이 잠금되었습니다. 잠시 후 다시 시도해주세요.","data":null}""")))
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
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
      {"status":200,"code":"AUTH_200_002","message":"로그아웃이 성공적으로 완료되었습니다.","data":null}""")))
    })

    @DeleteMapping("/logout")
    public ApiResponseDTO<String> logout(@RequestHeader("Authorization") String accessToken,
                                         @RequestHeader(name = "Refresh-Token") String refreshToken) {
        return authService.logout(accessToken, refreshToken);
    }

    /**
     * AccessToken 재발급
     */
    @Operation(summary = "AccessToken 재발급", description = "쿠키에 저장된 RefreshToken을 이용하여 AccessToken을 재발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
      {"status":200,"code":"AUTH_200_003","message":"RT값으로 새로운 AT생성에 성공했습니다.",
       "data":{"accessToken":"eyJhbGciOiJIUzI1NiIs..."}}"""))),
            @ApiResponse(responseCode = "401", description = "토큰 문제(없음/유효X/만료)",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
      {"status":401,"code":"AUTH_401_004","message":"토큰 유효하지 않음","data":null}""")))
    })

    @PostMapping("/reissue")
    public ApiResponseDTO<Map<String, String>> reissueAccessToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return authService.reissueAccessToken(request, response);
    }
}
