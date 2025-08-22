package com.gyeongditor.storyfield.Controller;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.dto.UserDTO.LoginDTO;
import com.gyeongditor.storyfield.service.AuthService;
import com.gyeongditor.storyfield.swagger.AuthApi;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;

    @Override
    public ApiResponseDTO<Map<String, String>> login(@Valid LoginDTO loginDTO, HttpServletResponse response) {
        return authService.login(loginDTO.getEmail(), loginDTO.getPassword(), response);
    }

    @Override
    public ApiResponseDTO<String> logout(String accessToken, String refreshToken) {
        return authService.logout(accessToken, refreshToken);
    }

    @Override
    public ApiResponseDTO<Map<String, String>> reissueAccessToken(HttpServletRequest request, HttpServletResponse response) {
        return authService.reissueAccessToken(request, response);
    }
}
