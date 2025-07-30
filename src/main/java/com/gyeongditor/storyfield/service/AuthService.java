package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.Entity.CustomUserDetails;
import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.jwt.JwtTokenProvider;
import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.response.SuccessCode;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.security.core.AuthenticationException;

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
        try {
            // 사용자 인증
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            // 토큰 생성
            String accessToken = jwtTokenProvider.createToken(authentication);
            String refreshToken = jwtTokenProvider.createRefreshToken(authentication);
            String uuid = ((CustomUserDetails) authentication.getPrincipal()).getUserId().toString();

            // 로그인 성공 → 실패 횟수 초기화
            userDetailsService.processSuccessfulLogin(email);

            // 응답 헤더 구성
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            headers.add("Refresh-Token", refreshToken);
            headers.add("userUUID", uuid);

            return ApiResponseDTO.success(SuccessCode.AUTH_200_001, headers);

        } catch (AuthenticationException e) {
            // 계정 상태 체크 (잠김/비활성화)
            try {
                userDetailsService.handleAccountStatus(email);
            } catch (CustomException ce) {
                throw ce;
            }

            // 로그인 실패 (횟수 증가)
            userDetailsService.processFailedLogin(email);
            int remainingAttempts = userDetailsService.getRemainingLoginAttempts(email);

            throw new CustomException(
                    ErrorCode.AUTH_401_009,
                    "이메일 또는 비밀번호가 올바르지 않습니다. 앞으로 " + remainingAttempts + "번 실패 시 계정이 잠깁니다."
            );
        }
    }

    /**
     * 로그아웃 (Refresh Token 블랙리스트 처리)
     */
    public ApiResponseDTO<String> logout(String token) {
        boolean success = jwtTokenProvider.blacklistRefreshToken(token);

        if (!success) {
            throw new CustomException(
                    ErrorCode.SERVER_500_001,
                    "로그아웃 처리 중 오류가 발생했습니다."
            );
        }

        return ApiResponseDTO.success(SuccessCode.AUTH_200_002, "로그아웃 성공");
    }
}
