package com.gyeongditor.storyfield.swagger.api;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.dto.UserDTO.LoginDTO;
import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.response.SuccessCode;
import com.gyeongditor.storyfield.swagger.config.ApiErrorResponse;
import com.gyeongditor.storyfield.swagger.config.ApiSuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Auth", description = "인증")
@RequestMapping("/api/auth")
public interface AuthApi {

    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호를 통해 로그인합니다.",
            security = {} // 로그인은 인증 불필요
    )
    @ApiSuccessResponse(
            SuccessCode.AUTH_200_001
    )
    @ApiErrorResponse({
            ErrorCode.AUTH_401_009, // 아이디/비밀번호 불일치
            ErrorCode.USER_403_003, // 계정 미활성 (이메일 인증 미완료)
            ErrorCode.USER_404_001, // 존재하지 않는 계정
            ErrorCode.USER_423_002  // 계정 잠금
    })
    @PostMapping("/login")
    ApiResponseDTO<Map<String, String>> login(
            @Valid @RequestBody LoginDTO loginDTO,
            HttpServletResponse response
    );

    @Operation(
            summary = "로그아웃",
            description = "Refresh Token을 통해 로그아웃합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")} // 인증 필요
    )
    @ApiSuccessResponse(
            SuccessCode.AUTH_200_002
    )
    @ApiErrorResponse({
            ErrorCode.AUTH_401_003, // 토큰 없음
            ErrorCode.AUTH_401_004, // 토큰 유효하지 않음
            ErrorCode.AUTH_401_005  // 토큰 만료
    })
    @DeleteMapping("/logout")
    ApiResponseDTO<String> logout(
            @RequestHeader("Authorization") String accessToken,
            @RequestHeader(name = "Refresh-Token") String refreshToken
    );

    @Operation(
            summary = "AccessToken 재발급",
            description = "쿠키에 저장된 RefreshToken을 이용하여 AccessToken을 재발급합니다.",
            security = {} // Refresh 기반 재발급은 보통 public 엔드포인트
    )
    @ApiSuccessResponse(
            SuccessCode.AUTH_200_007
    )     @ApiErrorResponse({
            ErrorCode.AUTH_401_003, // 토큰 없음
            ErrorCode.AUTH_401_004, // 토큰 유효하지 않음
            ErrorCode.AUTH_401_005, // 토큰 만료
            ErrorCode.AUTH_401_011  // 인증 토큰 만료됨 (세부 구분)
    })
    @PostMapping("/reissue")
    ApiResponseDTO<Map<String, String>> reissueAccessToken(
            HttpServletRequest request,
            HttpServletResponse response
    );

    @Operation(
            summary = "토큰 검증",
            description = "AccessToken의 유효 여부를 Boolean 값으로 반환합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiSuccessResponse(
            SuccessCode.AUTH_200_008 // 신규 SuccessCode 추가 필요
    )
    @ApiErrorResponse({
            ErrorCode.AUTH_401_003, // 토큰 없음
            ErrorCode.AUTH_401_004  // 토큰 유효하지 않음
    })
    @PostMapping("/verify")
    ApiResponseDTO<Boolean> verifyToken(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    );
}
