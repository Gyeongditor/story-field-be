package com.gyeongditor.storyfield.oauth.handler;

import com.gyeongditor.storyfield.Entity.CustomOAuth2User;
import com.gyeongditor.storyfield.Entity.CustomUserDetails;
import com.gyeongditor.storyfield.Entity.User;
import com.gyeongditor.storyfield.jwt.JwtTokenProvider;
import com.gyeongditor.storyfield.oauth.user.OAuth2UserInfo;
import com.gyeongditor.storyfield.oauth.user.OAuth2UserInfoFactory;
import com.gyeongditor.storyfield.service.CustomOAuth2UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;


import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomOAuth2UserService oAuth2UserService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // 소셜 로그인 사용자 정보
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String socialType = customOAuth2User.getSocialType();

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory
                .getOAuth2UserInfo(socialType, customOAuth2User.getAttributes());

        // 사용자 조회 or 신규 등록
        User user = oAuth2UserService.getUserByOAuth2UserInfo(userInfo, socialType);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        // Security 인증 객체 갱신
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 토큰 생성
        String accessToken = jwtTokenProvider.createToken(auth);
        String refreshToken = jwtTokenProvider.createRefreshToken(auth);

        // Access Token → 헤더로 전달
        response.setHeader("Authorization", "Bearer " + accessToken);

        // Refresh Token → HttpOnly 쿠키로 전달
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);      // JS 접근 불가
        refreshTokenCookie.setSecure(false);       // HTTPS 환경이라면 true
        refreshTokenCookie.setPath("/");           // 전체 경로에서 전송
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7일 유지

        response.addCookie(refreshTokenCookie);

        // Security redirect 제거
        clearAuthenticationAttributes(request);

        // 프론트엔드로 redirect (나중에 주소만 교체)
        String frontendUrl = "http://localhost:3000/login/success";
        response.sendRedirect(frontendUrl);
    }
}
