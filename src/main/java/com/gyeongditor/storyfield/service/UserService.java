package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.Entity.User;
import com.gyeongditor.storyfield.dto.UserDTO.SignUpDTO;
import com.gyeongditor.storyfield.dto.UserDTO.UpdateUserDTO;
import com.gyeongditor.storyfield.dto.UserDTO.UserResponseDTO;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.jwt.JwtTokenProvider;
import com.gyeongditor.storyfield.repository.UserRepository;
import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.response.SuccessCode;
import jakarta.servlet.http.HttpServletRequest;
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
    private final JwtTokenProvider jwtTokenProvider;

    // ───────────────────── 회원가입 ─────────────────────
    public ApiResponseDTO<UserResponseDTO> signUp(SignUpDTO signUpDTO) {
        // 이메일 중복 검사
        if (isEmailAlreadyExists(signUpDTO.getEmail())) {
            throw new CustomException(ErrorCode.USER_409_001, "이미 등록된 이메일입니다.");
        }
        
        // 닉네임 중복 검사 (추후 구현 시 사용)
        if (isUsernameAlreadyExists(signUpDTO.getUsername())) {
            throw new CustomException(ErrorCode.USER_409_002, "이미 사용 중인 닉네임입니다.");
        }

        String encodedPassword = encodePassword(signUpDTO.getPassword());
        String verificationToken = UUID.randomUUID().toString();

        try {
            User user = User.builder()
                    .email(signUpDTO.getEmail())
                    .username(signUpDTO.getUsername())
                    .password(encodedPassword)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .enabled(false) // 이메일 인증 전까지는 비활성화
                    .mailVerificationToken(verificationToken)
                    .build();

            userRepository.save(user);
            sendEmail(user.getEmail(), verificationToken, "회원가입 이메일 인증");

            UserResponseDTO dto = new UserResponseDTO(user.getEmail(), user.getUsername());
            return ApiResponseDTO.success(SuccessCode.USER_201_001, dto);
            
        } catch (Exception e) {
            throw new CustomException(ErrorCode.SERVER_500_001, "회원가입 처리 중 서버 오류가 발생했습니다.");
        }
    }

    // ───────────────────── 이메일 인증 ─────────────────────
    public ApiResponseDTO<UserResponseDTO> verifyEmail(String token) {
        User user = findUserByVerificationToken(token);
        user.enableAccount();
        userRepository.save(user);

        UserResponseDTO dto = new UserResponseDTO(user.getEmail(), user.getUsername());
        return ApiResponseDTO.success(SuccessCode.USER_200_003, dto);
    }

    // ───────────────────── 회원 조회 (accessToken 기반) ─────────────────────
    public ApiResponseDTO<UserResponseDTO> getUserByAccessToken(HttpServletRequest request) {
        String accessToken = jwtTokenProvider.resolveToken(request);
        User user = getUserFromToken(accessToken);


        UserResponseDTO dto = new UserResponseDTO(user.getEmail(), user.getUsername());
        return ApiResponseDTO.success(SuccessCode.USER_200_001, dto);
    }

    // ───────────────────── 회원 수정 (accessToken 기반) ─────────────────────
    public ApiResponseDTO<UserResponseDTO> updateUserByAccessToken(HttpServletRequest request, UpdateUserDTO updateUserDTO) {
        String accessToken = jwtTokenProvider.resolveToken(request);
        User user = getUserFromToken(accessToken);

        if (!user.getEmail().equals(updateUserDTO.getEmail())) {
            // 이메일 변경 시 인증 로직 추가 가능
        }

        String verificationToken = UUID.randomUUID().toString();
        user.updateUser(updateUserDTO, passwordEncoder, verificationToken);
        sendEmail(user.getEmail(), verificationToken, "회원 정보 수정용 이메일 인증");

        userRepository.save(user);
        UserResponseDTO dto = new UserResponseDTO(user.getEmail(), user.getUsername());
        return ApiResponseDTO.success(SuccessCode.USER_200_002, dto);
    }

    // ───────────────────── 회원 삭제 (accessToken 기반) ─────────────────────
    public ApiResponseDTO<Void> deleteUserByAccessToken(HttpServletRequest request) {
        String accessToken = jwtTokenProvider.resolveToken(request);
        User user = getUserFromToken(accessToken);
        userRepository.deleteById(user.getUserId());
        return ApiResponseDTO.success(SuccessCode.USER_204_001, null);
    }

    // ───────────────────── 내부 유틸 ─────────────────────
    public User getUserFromToken(String token) {
        jwtTokenProvider.validateOrThrow(token); // 유효성 검사 → 실패 시 예외
        String email = jwtTokenProvider.getEmail(token); // 이메일 추출

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_404_001, "사용자를 찾을 수 없습니다."));
    }

    private boolean isEmailAlreadyExists(String email) {
        return userRepository.existsByEmail(email);
    }
    
    private boolean isUsernameAlreadyExists(String username) {
        // TODO: UserRepository에 existsByUsername 메서드 추가 필요
        // return userRepository.existsByUsername(username);
        return false; // 임시로 false 반환 (추후 구현 시 USER_409_002 ErrorCode 사용됨)
    }

    private void sendEmail(String email, String verificationToken, String subject) {
        String url = "http://localhost:9080/api/user/verify/" + verificationToken;
        mailService.sendEmail(email, url, subject);
    }

    private User findUserByVerificationToken(String token) {
        return userRepository.findByMailVerificationToken(token)
                .orElseThrow(() -> new CustomException(ErrorCode.RES_404_001, "유효한 인증 토큰이 없습니다."));
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}
