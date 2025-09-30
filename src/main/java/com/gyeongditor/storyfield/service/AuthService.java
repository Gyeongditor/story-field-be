package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

public interface AuthService {

    ApiResponseDTO<Map<String, String>> login(String email, String password, HttpServletResponse response);

    void handleLoginFailure(String email);

    ApiResponseDTO<String> logout(String accessToken, String refreshToken);

    ApiResponseDTO<Map<String, String>> reissueAccessToken(HttpServletRequest request, HttpServletResponse response);

    String extractAccessToken(HttpServletRequest request);

    public ApiResponseDTO<Boolean> verifyToken(String authorizationHeader);
}