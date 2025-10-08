package com.gyeongditor.storyfield.service.impl;

import com.gyeongditor.storyfield.Entity.CustomUserDetails;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.jwt.JwtTokenProvider;
import com.gyeongditor.storyfield.repository.JwtTokenRedisRepository;
import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.response.SuccessCode;
import com.gyeongditor.storyfield.service.AuthService;
import com.gyeongditor.storyfield.service.CustomUserDetailsService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenRedisRepository jwtTokenRedisRepository;

    @Override
    public ApiResponseDTO<Map<String, String>> login(String email, String password, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        String accessToken = jwtTokenProvider.createToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);
        String uuid = ((CustomUserDetails) authentication.getPrincipal()).getUserId().toString();

        userDetailsService.processSuccessfulLogin(email);

        jwtTokenRedisRepository.saveRefreshToken(uuid, refreshToken, jwtTokenProvider.refreshTokenValiditySeconds);

        ResponseCookie rtCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofSeconds(jwtTokenProvider.refreshTokenValiditySeconds))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, rtCookie.toString());

        response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        response.addHeader("userUUID", uuid);

        Map<String, String> data = new HashMap<>();
        data.put("로그인 상태", "성공");

        return ApiResponseDTO.success(SuccessCode.AUTH_200_001, data);
    }

    @Override
    public void handleLoginFailure(String email) {
        userDetailsService.handleAccountStatus(email);
        userDetailsService.processFailedLogin(email);
        int remaining = userDetailsService.getRemainingLoginAttempts(email);

        throw new CustomException(
                ErrorCode.AUTH_401_009,
                "이메일 또는 비밀번호가 올바르지 않습니다. 앞으로 " + remaining + "번 실패 시 계정이 잠깁니다."
        );
    }

    @Override
    public ApiResponseDTO<String> logout(String accessToken, String refreshToken) {
        String pureAccessToken = extractToken(accessToken);
        jwtTokenProvider.invalidateTokensOrThrow(pureAccessToken, refreshToken);
        return ApiResponseDTO.success(SuccessCode.AUTH_200_002, "로그아웃 성공");
    }

    @Override
    public ApiResponseDTO<Map<String, String>> reissueAccessToken(HttpServletRequest request,
                                                                  HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        String uuid = validateRefreshTokenWithRedis(refreshToken);
        Authentication auth = createAuthenticationFromUUID(uuid);
        handleRotationPolicy(auth, uuid, response);
        String newAccessToken = jwtTokenProvider.createToken(auth);
        addAccessTokenHeaders(response, newAccessToken, uuid);

        Map<String, String> data = new HashMap<>();
        data.put("로그인 상태", "성공");
        data.put("AccessToken", newAccessToken);
        return ApiResponseDTO.success(SuccessCode.AUTH_200_007, data);
    }

    @Override
    public String extractAccessToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.AUTH_401_003, "토큰이 존재하지 않습니다.");
        }
        return authorizationHeader.substring(7).trim();
    }

    // 내부 유틸

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new CustomException(ErrorCode.AUTH_401_010, "Authorization 헤더가 비어있습니다.");
        }
        if (authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        if (authorizationHeader.trim().isEmpty()) {
            throw new CustomException(ErrorCode.AUTH_401_003, "토큰이 비어있습니다.");
        }
        return authorizationHeader;
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        throw new CustomException(ErrorCode.AUTH_401_003, "RefreshToken이 존재하지 않습니다.");
    }

    private String validateRefreshTokenWithRedis(String refreshToken) {
        String email = jwtTokenProvider.getEmail(refreshToken);
        CustomUserDetails userDetails =
                (CustomUserDetails) userDetailsService.loadUserByUsername(email);
        String uuid = userDetails.getUserId().toString();

        String storedRT = jwtTokenRedisRepository.getRefreshToken(uuid);
        if (storedRT == null) {
            throw new CustomException(ErrorCode.AUTH_401_005, "RefreshToken이 만료되었거나 존재하지 않습니다.");
        }
        if (!storedRT.equals(refreshToken)) {
            throw new CustomException(ErrorCode.AUTH_401_004, "RefreshToken이 유효하지 않습니다.");
        }
        return uuid;
    }

    private Authentication createAuthenticationFromUUID(String uuid) {
        CustomUserDetails userDetails =
                (CustomUserDetails) userDetailsService.loadUserByUUID(uuid);
        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }

    private void handleRotationPolicy(Authentication auth, String uuid, HttpServletResponse response) {
        boolean rotationEnabled = true;
        if (rotationEnabled) {
            String newRT = jwtTokenProvider.createRefreshToken(auth);
            jwtTokenRedisRepository.saveRefreshToken(uuid, newRT,
                    jwtTokenProvider.getRefreshTokenValiditySeconds());

            ResponseCookie rtCookie = ResponseCookie.from("refreshToken", newRT)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Strict")
                    .path("/")
                    .maxAge(Duration.ofSeconds(jwtTokenProvider.getRefreshTokenValiditySeconds()))
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, rtCookie.toString());
        }
    }

    private void addAccessTokenHeaders(HttpServletResponse response, String accessToken, String uuid) {
        response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        response.addHeader("userUUID", uuid);
    }

    @Override
    public ApiResponseDTO<Boolean> verifyToken(String authorizationHeader) {
        // 1. Authorization 헤더에서 Bearer 토큰만 추출
        String token = extractToken(authorizationHeader);

        // 2. JwtTokenProvider 검증 (유효하지 않으면 CustomException 발생 → GlobalExceptionHandler 처리)
        jwtTokenProvider.validateOrThrow(token);

        // 3. 여기까지 오면 토큰이 유효한 것 → true 반환
        return ApiResponseDTO.success(SuccessCode.AUTH_200_008, true);
    }
}