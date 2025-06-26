package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.Entity.User;
import com.gyeongditor.storyfield.dto.UserDTO.UserSignupRequestDTO;
import com.gyeongditor.storyfield.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public String signup(UserSignupRequestDTO request) {
        // 1. loginId 중복 체크
        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new IllegalArgumentException("이미 존재하는 로그인 아이디입니다.");
        }

        // 2. 비밀번호 암호화
        String encodedPw = passwordEncoder.encode(request.getUserPw());

        // 3. User 엔티티 생성
        User user = User.builder()
                .userId(UUID.randomUUID())
                .loginId(request.getLoginId())
                .userPw(encodedPw)
                .name(request.getName())
                .age(request.getAge())
                .sex(request.getSex())
                .userEmail(request.getUserEmail())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 4. 저장
        userRepository.save(user);

        return user.getUserId().toString();
    }

}
