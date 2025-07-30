package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.Entity.CustomUserDetails;
import com.gyeongditor.storyfield.Entity.User;
import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.repository.UserRepository;
import com.gyeongditor.storyfield.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 1;

    /**
     * 이메일(userId) 기준으로 사용자 조회
     */
    @Override
    public UserDetails loadUserByUsername(String userId) {
        // 사용자 조회 실패 시 CustomException
        User user = userRepository.findByEmail(userId)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.USER_404_002,
                        "해당 userId(" + userId + ")를 가진 사용자를 찾을 수 없습니다."
                ));

        // UserDetails 반환
        return new CustomUserDetails(
                user.getUserId(),
                user.getEmail(),
                user.getPassword(),
                user.getUsername(),
                user.isAccountNonExpired(),
                user.isAccountNonLocked(),
                user.isCredentialsNonExpired(),
                user.isEnabled(),
                user.getFailedLoginAttempts(),
                user.getLockTime(),
                user.getSocialType(),
                user.getSocialId(),
                Collections.emptyList()
        );
    }

    /**
     * 계정 상태 체크
     */
    public void handleAccountStatus(String email) {
        User user = findUserByEmail(email);

        // 계정 활성화되지 않음
        if (!user.isEnabled()) {
            throw new CustomException(ErrorCode.AUTH_403_002);
        }

        // 계정 잠김
        if (!user.isAccountNonLocked()) {
            throw new CustomException(
                    ErrorCode.USER_423_002,
                    "계정이 잠금되었습니다. " + user.getLockTime().plusMinutes(LOCKOUT_MINUTES) + " 이후에 다시 시도해주세요."
            );
        }
    }

    /**
     * 로그인 실패 처리
     */
    public void processFailedLogin(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.incrementFailedLoginAttempts();
            if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.lockAccount();
            }
            userRepository.save(user);
        });
    }

    /**
     * 로그인 성공 처리
     */
    public void processSuccessfulLogin(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.resetFailedLoginAttempts();
            userRepository.save(user);
        });
    }

    /**
     * 남은 로그인 가능 횟수
     */
    public int getRemainingLoginAttempts(String email) {
        return userRepository.findByEmail(email)
                .map(User::getFailedLoginAttempts)
                .filter(attempts -> attempts < MAX_FAILED_ATTEMPTS)
                .map(attempts -> MAX_FAILED_ATTEMPTS - attempts + 1)
                .orElse(1);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_404_002));
    }
}
