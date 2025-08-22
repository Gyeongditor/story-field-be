package com.gyeongditor.storyfield.Controller;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.dto.UserDTO.SignUpDTO;
import com.gyeongditor.storyfield.dto.UserDTO.UpdateUserDTO;
import com.gyeongditor.storyfield.dto.UserDTO.UserResponseDTO;
import com.gyeongditor.storyfield.service.UserService;
import com.gyeongditor.storyfield.swagger.UserApi;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    public ApiResponseDTO<UserResponseDTO> signUp(SignUpDTO signUpDTO) {
        return userService.signUp(signUpDTO);
    }

    @Override
    public ApiResponseDTO<UserResponseDTO> getUser(HttpServletRequest request) {
        return userService.getUserByAccessToken(request);
    }

    @Override
    public ApiResponseDTO<UserResponseDTO> updateUser(HttpServletRequest request, UpdateUserDTO updateUserDTO) {
        return userService.updateUserByAccessToken(request, updateUserDTO);
    }

    @Override
    public ApiResponseDTO<Void> deleteUser(HttpServletRequest request) {
        return userService.deleteUserByAccessToken(request);
    }

    @Override
    public ApiResponseDTO<UserResponseDTO> verifyEmail(String token) {
        return userService.verifyEmail(token);
    }
}
