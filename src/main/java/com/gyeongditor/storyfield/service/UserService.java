package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.Entity.User;
import com.gyeongditor.storyfield.dto.UserDTO.SignUpDTO;
import com.gyeongditor.storyfield.dto.UserDTO.UpdateUserDTO;
import com.gyeongditor.storyfield.dto.UserDTO.UserResponseDTO;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.repository.UserRepository;
import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    /**
     * 회원 가입
     */
    public ApiResponseDTO<UserResponseDTO> signUp(SignUpDTO signUpDTO) {
        if (isEmailAlreadyExists(signUpDTO.getEmail())) {
            throw new CustomException(ErrorCode.USER_409_001, "이미 등록된 이메일입니다.");
        }

        String encodedPassword = encodePassword(signUpDTO.getPassword());
        String verificationToken = UUID.randomUUID().toString();

        User user = User.builder()
                .email(signUpDTO.getEmail())
                .username(signUpDTO.getUsername())
                .password(encodedPassword)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .mailVerificationToken(verificationToken)
                .build();

        sendEmail(user.getEmail(), verificationToken, "회원가입 이메일 인증");
        userRepository.save(user);

        UserResponseDTO dto = new UserResponseDTO(user.getUserId(), user.getUsername(), user.getEmail());
        return ApiResponseDTO.success(SuccessCode.USER_201_001, dto);
    }

    private boolean isEmailAlreadyExists(String email) {
        return userRepository.existsByEmail(email);
    }

    private void sendEmail(String email, String verificationToken, String subject) {
        String verificationUrl = "http://localhost:9080/user/verify/" + verificationToken;
        mailService.sendEmail(email, verificationUrl, subject);
    }

    private User findUserByVerificationToken(String token) {
        return userRepository.findByMailVerificationToken(token)
                .orElseThrow(() -> new CustomException(ErrorCode.RES_404_001, "유효한 토큰이 없습니다."));
    }

    /**
     * 이메일 인증
     */
    public ApiResponseDTO<UserResponseDTO> verifyEmail(String token) {
        User user = findUserByVerificationToken(token);
        user.enableAccount();
        userRepository.save(user);

        UserResponseDTO dto = new UserResponseDTO(user.getEmail(), user.getUsername());
        return ApiResponseDTO.success(SuccessCode.USER_200_003, dto);
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * 회원 정보 조회
     */
    public ApiResponseDTO<UserResponseDTO> getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_404_001, "사용자를 찾을 수 없습니다."));

        UserResponseDTO dto = new UserResponseDTO(user.getUserId(), user.getEmail(), user.getUsername());
        return ApiResponseDTO.success(SuccessCode.USER_200_001, dto);
    }

    /**
     * 회원 정보 수정
     */
    public ApiResponseDTO<UserResponseDTO> updateUser(UUID userId, UpdateUserDTO updateUserDTO) {
        return userRepository.findById(userId).map(user -> {
            if (!user.getEmail().equals(updateUserDTO.getEmail())) {
                // 이메일 변경 시 새 이메일로 인증 필요
                // 중복 검증도 추가 가능
                // verifyEmail(updateUserDTO.getEmail()); <-- 기존 이메일 인증 로직
            }

            String verificationToken = UUID.randomUUID().toString();
            user.updateUser(updateUserDTO, passwordEncoder, verificationToken);
            sendEmail(user.getEmail(), verificationToken, "회원 정보 수정용 이메일 인증");

            userRepository.save(user);
            UserResponseDTO dto = new UserResponseDTO(user.getEmail(), user.getUsername());
            return ApiResponseDTO.success(SuccessCode.USER_200_002, dto);

        }).orElseThrow(() -> new CustomException(ErrorCode.USER_404_001, "사용자를 찾을 수 없습니다."));
    }

    /**
     * 회원 삭제
     */
    public ApiResponseDTO<Void> deleteUser(UUID userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            return ApiResponseDTO.success(SuccessCode.USER_204_001, null);
        } else {
            throw new CustomException(ErrorCode.USER_404_001, "사용자를 찾을 수 없습니다.");
        }
    }
}
