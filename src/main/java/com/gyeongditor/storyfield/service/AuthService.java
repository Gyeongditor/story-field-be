package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.Entity.CustomUserDetails;
import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.jwt.JwtTokenProvider;
import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.response.SuccessCode;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

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
    public ApiResponseDTO<HttpHeaders> login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        String accessToken = jwtTokenProvider.createToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);
        String uuid = ((CustomUserDetails) authentication.getPrincipal()).getUserId().toString();

        // 로그인 성공 → 실패 횟수 초기화
        userDetailsService.processSuccessfulLogin(email);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        headers.add("Refresh-Token", refreshToken);
        headers.add("userUUID", uuid);

        return ApiResponseDTO.success(SuccessCode.AUTH_200_001, headers);
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
}

