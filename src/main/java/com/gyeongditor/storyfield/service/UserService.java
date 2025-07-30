package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.Entity.User;
import com.gyeongditor.storyfield.dto.UserDTO.SignUpDTO;
import com.gyeongditor.storyfield.dto.UserDTO.UpdateUserDTO;
import com.gyeongditor.storyfield.dto.UserDTO.UserResponseDTO;
import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.repository.UserRepository;
import com.gyeongditor.storyfield.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
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
    public UserResponseDTO signUp(SignUpDTO signUpDTO) {
        // 이메일 중복 체크
        if (isEmailAlreadyExists(signUpDTO.getEmail())) {
            throw new CustomException(ErrorCode.USER_409_001, "이미 등록된 이메일입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = encodePassword(signUpDTO.getPassword());
        String verificationToken = UUID.randomUUID().toString();

        // 회원 정보 생성
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

        // 이메일 전송
        sendEmail(user.getEmail(), verificationToken, "회원가입 이메일 인증");

        // 회원 저장
        userRepository.save(user);

        return new UserResponseDTO(user.getUserId(), user.getUsername(), user.getEmail());
    }

    /**
     * 이메일 중복 체크
     */
    public boolean isEmailAlreadyExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 이메일 전송
     */
    private void sendEmail(String email, String verificationToken, String subject) {
        String verificationUrl = "http://localhost:9080/user/verify/" + verificationToken;
        mailService.sendEmail(email, verificationUrl, subject);
    }

    /**
     * 이메일 토큰으로 사용자 조회
     */
    private User findUserByVerificationToken(String token) {
        return userRepository.findByMailVerificationToken(token)
                .orElseThrow(() -> new CustomException(ErrorCode.RES_404_001, "유효한 토큰이 없습니다."));
    }

    /**
     * 이메일 인증
     */
    public UserResponseDTO verifyEmail(String token) {
        User user = findUserByVerificationToken(token);
        user.enableAccount(); // 계정 활성화
        userRepository.save(user);
        return new UserResponseDTO(user.getEmail(), user.getUsername());
    }

    /**
     * 비밀번호 암호화
     */
    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * 회원 정보 조회
     */
    public UserResponseDTO getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_404_001, "사용자를 찾을 수 없습니다."));

        return new UserResponseDTO(user.getUserId(), user.getEmail(), user.getUsername());
    }

    /**
     * 회원 정보 수정
     */
    public UserResponseDTO updateUser(UUID userId, UpdateUserDTO updateUserDTO) {
        return userRepository.findById(userId).map(user -> {
            if (!user.getEmail().equals(updateUserDTO.getEmail())) {
                // 새 이메일 인증 (이메일 중복 검증은 여기 추가 가능)
                verifyEmail(updateUserDTO.getEmail());
            }

            String verificationToken = UUID.randomUUID().toString();
            user.updateUser(updateUserDTO, passwordEncoder, verificationToken);
            sendEmail(user.getEmail(), verificationToken, "회원 정보 수정용 이메일 인증");

            userRepository.save(user);
            return new UserResponseDTO(user.getEmail(), user.getUsername());
        }).orElseThrow(() -> new CustomException(ErrorCode.USER_404_001, "사용자를 찾을 수 없습니다."));
    }

    /**
     * 회원 삭제
     */
    public void deleteUser(UUID userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
        } else {
            throw new CustomException(ErrorCode.USER_404_001, "사용자를 찾을 수 없습니다.");
        }
    }
}
