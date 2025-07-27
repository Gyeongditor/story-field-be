package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.Entity.User;
import com.gyeongditor.storyfield.dto.UserDTO.UpdateUserDTO;
import com.gyeongditor.storyfield.repository.UserRepository;
import com.gyeongditor.storyfield.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;


    public seungil.login_boilerplate.dto.UserResponseDTO signUp(seungil.login_boilerplate.dto.SignUpDTO signUpDTO) {
        // 이메일 중복 체크
        if (isEmailAlreadyExists(signUpDTO.getEmail())) {
            throw new IllegalStateException("이미 등록된 이메일 입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = encodePassword(signUpDTO.getPassword());

        String verificationToken = UUID.randomUUID().toString();

        // 회원 정보 생성
        User user = User.builder()
                .email(signUpDTO.getEmail())
                .userName(signUpDTO.getUsername())
                .password(encodedPassword)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .mailVerificationToken(verificationToken)
                .build();

        sendEmail(user.getEmail(), verificationToken, "회원가입 이메일 인증");
        // 회원 저장
        userRepository.save(user);

        // UserResponseDTO 생성
        return new seungil.login_boilerplate.dto.UserResponseDTO(user.getUserId(), user.getUserName(), user.getEmail());
    }

    // 이메일 중복 체크 메서드
    public boolean isEmailAlreadyExists(String email){
        return userRepository.existsByEmail(email);
    }

    // 이메일 전송 메서드
    private void sendEmail(String email, String verificationToken, String subject) {
        String verificationUrl = "http://localhost:9080/user/verify/" + verificationToken;
        mailService.sendEmail(email, verificationUrl, subject);
    }

    // 이메일 토큰을 사용하여 사용자 조회
    private User findUserByVerificationToken(String token) {
        return userRepository.findByMailVerificationToken(token)
                .orElseThrow(() -> new IllegalStateException("유효한 토큰이 없습니다."));
    }

    // 이메일 검증
    public seungil.login_boilerplate.dto.UserResponseDTO verifyEmail(String token) {
        User user = findUserByVerificationToken(token);
        user.enableAccount(); // 엔티티 메서드 사용
        userRepository.save(user);
        return new seungil.login_boilerplate.dto.UserResponseDTO(user.getEmail(), user.getUserName());
    }

    // 비밀번호 암호화 메서드
    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    // 회원 정보 조회
    public seungil.login_boilerplate.dto.UserResponseDTO getUserById(UUID userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            return new seungil.login_boilerplate.dto.UserResponseDTO(user.get().getUserId(), user.get().getEmail(), user.get().getUserName());
        } else {
            throw new IllegalStateException("사용자를 찾을 수 없습니다.");
        }
    }

    // 회원 정보 수정
    public seungil.login_boilerplate.dto.UserResponseDTO updateUser(UUID userId, UpdateUserDTO updateUserDTO) {
        return userRepository.findById(userId).map(user -> {
            if (!user.getEmail().equals(updateUserDTO.getEmail())) {
                verifyEmail(updateUserDTO.getEmail());
            }
            String verificationToken = UUID.randomUUID().toString();
            user.updateUser(updateUserDTO, passwordEncoder,verificationToken);
            sendEmail(user.getEmail(), verificationToken, "회원 정보 수정용 이메일 인증");

            userRepository.save(user);
            return new seungil.login_boilerplate.dto.UserResponseDTO(user.getEmail(), user.getUserName());
        }).orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));
    }

    // 회원 삭제
    public void deleteUser(UUID userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
        } else {
            throw new IllegalStateException("사용자를 찾을 수 없습니다.");
        }
    }
}