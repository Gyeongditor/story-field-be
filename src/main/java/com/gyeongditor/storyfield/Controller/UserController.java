package com.gyeongditor.storyfield.Controller;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.dto.UserDTO.SignUpDTO;
import com.gyeongditor.storyfield.dto.UserDTO.UpdateUserDTO;
import com.gyeongditor.storyfield.dto.UserDTO.UserResponseDTO;
import com.gyeongditor.storyfield.service.UserService;
import com.gyeongditor.storyfield.swagger.UserApi;
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
    public ApiResponseDTO<UserResponseDTO> getUser(String authorizationHeader) {
        String accessToken = authorizationHeader.replace("Bearer ", "");
        return userService.getUserByAccessToken(accessToken);
    }

    @Override
    public ApiResponseDTO<UserResponseDTO> updateUser(String authorizationHeader, UpdateUserDTO updateUserDTO) {
        String accessToken = authorizationHeader.replace("Bearer ", "");
        return userService.updateUserByAccessToken(accessToken, updateUserDTO);
    }

    @Override
    public ApiResponseDTO<Void> deleteUser(String authorizationHeader) {
        String accessToken = authorizationHeader.replace("Bearer ", "");
        return userService.deleteUserByAccessToken(accessToken);
    }

    @Override
    public ApiResponseDTO<UserResponseDTO> verifyEmail(String token) {
        return userService.verifyEmail(token);
    }
}
