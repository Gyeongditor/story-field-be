package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.Entity.CustomUserDetails;
import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.security.core.AuthenticationException;
import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.dto.ApiResponse;
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
    public HttpHeaders login(String email, String password) {
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

            return headers;

        } catch (AuthenticationException e) {
            // 계정 상태 체크 (잠김/비활성화)
            try {
                userDetailsService.handleAccountStatus(email);
            } catch (CustomException ce) {
                // 계정 상태 예외는 그대로 GlobalResponseHandler로 전달
                throw ce;
            }

            // 로그인 실패 (횟수 증가, 잠금 여부 체크)
            userDetailsService.processFailedLogin(email);

            // 남은 로그인 가능 횟수 계산
            int remainingAttempts = userDetailsService.getRemainingLoginAttempts(email);

            // 실패 예외 던지기 (남은 시도 횟수 포함)
            throw new CustomException(
                    ErrorCode.AUTH_401_009,
                    "이메일 또는 비밀번호가 올바르지 않습니다. 앞으로 " + remainingAttempts + "번 실패 시 계정이 잠깁니다."
            );
        }
    }

    /**
     * 남은 로그인 가능 횟수 반환
     */
    public int getRemainingLoginAttempts(String email) {
        return userDetailsService.getRemainingLoginAttempts(email);
    }

    /**
     * 로그아웃 (Refresh Token 블랙리스트 처리)
     */
    public void logout(String token) {
        boolean success = jwtTokenProvider.blacklistRefreshToken(token);

        if (!success) {
            throw new CustomException(
                    ErrorCode.SERVER_500_001,
                    "로그아웃 처리 중 오류가 발생했습니다."
            );
        }
    }
}
