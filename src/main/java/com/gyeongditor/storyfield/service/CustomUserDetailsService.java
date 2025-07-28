package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.Entity.CustomUserDetails;
import com.gyeongditor.storyfield.Entity.User;
import com.gyeongditor.storyfield.exception.UserAccountLockedException;
import com.gyeongditor.storyfield.exception.UserNotEnabledException;
import com.gyeongditor.storyfield.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 1;

    @Override // userId을 기준으로 사용자를 로드하는 메서드
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(userId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 userId를 가진 사용자를 찾을 수 없습니다: " + userId));

        // 조회된 사용자 정보를 기반으로 CustomUserDetails 객체 생성 후 반환
        return new CustomUserDetails(
                user.getUserId(),         // UUID 추가
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
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일을 가진 사용자를 찾을 수 없습니다."));
    }

    public void handleAccountStatus(String email) {
        User user = findUserByEmail(email);

        // 계정이 활성화되지 않은 경우 예외 발생
        if (!user.isEnabled()) {
            throw new UserNotEnabledException("계정이 활성화되지 않았습니다. 이메일 인증을 완료해주세요.");
        }

        // 계정이 잠금된 경우 예외 발생
        if (!user.isAccountNonLocked()) {
            throw new UserAccountLockedException("계정이 잠금되었습니다. " + user.getLockTime().plusMinutes(LOCKOUT_MINUTES) + " 이후에 다시 시도해주세요.");
        }
    }

    public void processFailedLogin(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.incrementFailedLoginAttempts();
            if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.lockAccount();
            }
            userRepository.save(user);
        });
    }

    public void processSuccessfulLogin(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.resetFailedLoginAttempts();
            userRepository.save(user);
        });
    }

    public int getRemainingLoginAttempts(String email) {
        return userRepository.findByEmail(email)
                .map(User::getFailedLoginAttempts)
                .filter(attempts -> attempts < MAX_FAILED_ATTEMPTS) // 잠금 전까지만 표시
                .map(attempts -> MAX_FAILED_ATTEMPTS - attempts + 1)
                .orElse(1);
    }
}
