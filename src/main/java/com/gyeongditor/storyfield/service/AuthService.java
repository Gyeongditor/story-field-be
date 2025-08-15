package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.Entity.CustomUserDetails;
import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.jwt.JwtTokenProvider;
import com.gyeongditor.storyfield.repository.JwtTokenRedisRepository;
import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.response.SuccessCode;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
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
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenRedisRepository jwtTokenRedisRepository;

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

        // 3) Redis에 RefreshToken 저장
        // refreshTokenValiditySeconds는 JwtTokenProvider에서 @Value로 받은 값 사용
        jwtTokenRedisRepository.saveRefreshToken(uuid, refreshToken, jwtTokenProvider.refreshTokenValiditySeconds);

        // 4) HttpOnly 쿠키에 RT 저장
        ResponseCookie rtCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true) // HTTPS 환경 필수
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofSeconds(jwtTokenProvider.refreshTokenValiditySeconds))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, rtCookie.toString());

        // 5) 응답 헤더에 AT, UUID 추가
        response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        response.addHeader("userUUID", uuid);

        // 6) data에 로그인 상태만 담기 (또는 필요 시 AT·UUID 추가 가능)
        Map<String, String> data = new HashMap<>();
        data.put("로그인 상태", "성공");

        // 7) ApiResponseDTO로 반환
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

    /**
     * RefreshToken으로 AccessToken 재발급
     */
    public ApiResponseDTO<Map<String, String>> reissueAccessToken(HttpServletRequest request,
                                                                  HttpServletResponse response) {

        // 1) RefreshToken 추출(쿠키)
        String refreshToken = extractRefreshTokenFromCookie(request);

        // 2) Redis에서 RefreshToken 검증(존재 여부, 만료 여부)
        String uuid = validateRefreshTokenWithRedis(refreshToken);

        // 3) 유효하면 Authentication 생성 (로그인 방식)
        Authentication auth = createAuthenticationFromUUID(uuid);

        // 4) Rotation 정책 적용 시 새 RefreshToken 발급 후 Redis 갱신
        handleRotationPolicy(auth, uuid, response);

        // 5) AccessToken 발급
        String newAccessToken = jwtTokenProvider.createToken(auth);

        // 6) 응답 헤더에 AT, UUID 추가
        addAccessTokenHeaders(response, newAccessToken, uuid);

        // 7) 응답 바디 반환
        Map<String, String> data = new HashMap<>();
        data.put("로그인 상태", "성공");
        return ApiResponseDTO.success(SuccessCode.AUTH_200_007, data);
    }

    /** 1) 쿠키에서 RT 추출 */
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

    /** 2) Redis에서 RT 검증 */
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

    /** 3) uuid로 Authentication 생성 (로그인 방식 재사용) */
    private Authentication createAuthenticationFromUUID(String uuid) {
        CustomUserDetails userDetails =
                (CustomUserDetails) userDetailsService.loadUserByUUID(uuid);
        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }

    /** 4) Rotation 정책 적용 */
    private void handleRotationPolicy(Authentication auth, String uuid, HttpServletResponse response) {
        boolean rotationEnabled = true; // yml로 뺄 수 있음
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

    /** 6) 응답 헤더에 AT, UUID 추가 */
    private void addAccessTokenHeaders(HttpServletResponse response, String accessToken, String uuid) {
        response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        response.addHeader("userUUID", uuid);
    }
}

