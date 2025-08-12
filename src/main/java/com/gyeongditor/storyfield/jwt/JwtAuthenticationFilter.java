package com.gyeongditor.storyfield.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyeongditor.storyfield.Entity.CustomUserDetails;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String accessToken = jwtTokenProvider.resolveToken(request);

        try {
            if (accessToken != null) {
                jwtTokenProvider.validateOrThrow(accessToken);
                authenticateWithToken(accessToken, request);
            }

            chain.doFilter(request, response);

        } catch (CustomException ex) {
            writeJsonError(response, ex.getErrorCode(), ex.getCustomMessage());
        } catch (Exception ex) {
            writeJsonError(response, ErrorCode.AUTH_401_004, "유효하지 않은 인증 토큰입니다.");
        }
    }

    private void authenticateWithToken(String token, HttpServletRequest request) {
        String email = jwtTokenProvider.getEmail(token);
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private boolean tryRefreshTokenAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String refreshToken = jwtTokenProvider.resolveRefreshToken(request);

            jwtTokenProvider.validateRefreshOrThrow(refreshToken);

            if (jwtTokenProvider.isRefreshTokenBlacklisted(refreshToken)) {
                throw new CustomException(ErrorCode.AUTH_401_008, "블랙리스트에 등록된 리프레시 토큰입니다.");
            }

            String newAccessToken = jwtTokenProvider.createTokenFromRefreshToken(refreshToken);
            response.setHeader("Authorization", "Bearer " + newAccessToken);
            authenticateWithToken(newAccessToken, request);
            return true;

        } catch (CustomException ex) {
            writeJsonError(response, ex.getErrorCode(), ex.getCustomMessage());
            return false;
        }
    }

    private void writeJsonError(HttpServletResponse response, ErrorCode errorCode, String customMessage) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        ApiResponseDTO<Object> body = ApiResponseDTO.error(errorCode, customMessage);
        String json = objectMapper.writeValueAsString(body);
        response.getWriter().write(json);
    }
}
