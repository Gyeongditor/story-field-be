package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.Entity.User;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.dto.UserDTO.SignUpDTO;
import com.gyeongditor.storyfield.dto.UserDTO.UpdateUserDTO;
import com.gyeongditor.storyfield.dto.UserDTO.UserResponseDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface UserService {

    ApiResponseDTO<UserResponseDTO> signUp(SignUpDTO signUpDTO);

    ApiResponseDTO<UserResponseDTO> verifyEmail(String token);

    ApiResponseDTO<UserResponseDTO> getUserByAccessToken(HttpServletRequest request);

    ApiResponseDTO<UserResponseDTO> updateUserByAccessToken(HttpServletRequest request, UpdateUserDTO updateUserDTO);

    ApiResponseDTO<Void> deleteUserByAccessToken(HttpServletRequest request);

    // 내부 유틸
    User getUserFromToken(String token);
}