package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.Entity.CustomUserDetails;
import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.jwt.JwtTokenProvider;
import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.response.SuccessCode;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    /**
     * 로그인 처리
     */
    public ApiResponseDTO<Map<String, String>> login(String email, String password, HttpServletResponse response) {
        // 1) 인증 처리
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        String accessToken = jwtTokenProvider.createToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);
        String uuid = ((CustomUserDetails) authentication.getPrincipal()).getUserId().toString();

        // 2) 로그인 성공 → 실패 횟수 초기화
        userDetailsService.processSuccessfulLogin(email);

        // 3) HttpOnly 쿠키에 RT 저장
        ResponseCookie rtCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true) // HTTPS 환경 필수
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofHours(14))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, rtCookie.toString());
        response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        response.addHeader("userUUID", uuid);

        // 4) data에 AT와 userUUID만 담기
        Map<String, String> data = new HashMap<>();
        data.put("로그인 상태", "성공");

        // 5) ApiResponseDTO로 반환
        return ApiResponseDTO.success(SuccessCode.AUTH_200_001, data);
    }

    /**
     * 로그인 실패 처리 (AuthenticationException 전용 핸들러에서 호출)
     */
    public void handleLoginFailure(String email) {
        // 계정 상태 확인
        userDetailsService.handleAccountStatus(email);

        // 실패 횟수 증가
        userDetailsService.processFailedLogin(email);
        int remaining = userDetailsService.getRemainingLoginAttempts(email);

        throw new CustomException(
                ErrorCode.AUTH_401_009,
                "이메일 또는 비밀번호가 올바르지 않습니다. 앞으로 " + remaining + "번 실패 시 계정이 잠깁니다."
        );
    }

    /**
     * 로그아웃
     */
    public ApiResponseDTO<String> logout(String refreshToken) {
        jwtTokenProvider.blacklistRefreshTokenOrThrow(refreshToken);
        return ApiResponseDTO.success(SuccessCode.AUTH_200_002, "로그아웃 성공");
    }

//    /**
//    * RT로 AT 재발급
//     */
//    public ApiResponseDTO<>


}

