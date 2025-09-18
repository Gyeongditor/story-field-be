package com.gyeongditor.storyfield.Entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final String userId; // 여기에 UUID 추가
    private final String email;
    private final String password;
    private final String username;
    private final boolean accountNonExpired; // 계정 만료 여부
    private final boolean accountNonLocked; // 계정 잠김 여부
    private final boolean credentialsNonExpired; // 자격 증명 만료 여부
    private final boolean enabled; // 계정 활성화 여부
    private final int failedLoginAttempts;
    private final LocalDateTime lockTime;
    private final String socialType; // 소셜 타입 (자체 로그인의 경우 Null)
    private final String socialId; // 소셜 ID  (자체 로그인의 경우 Null)
    private final Collection<? extends GrantedAuthority> authorities; // 사용자 권한 목록

    public CustomUserDetails(User user) {
        this.userId = user.getUserId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.username = user.getUsername(); // User 엔티티에서 username 매핑
        this.accountNonExpired = true; // 기본값
        this.accountNonLocked = true;
        this.credentialsNonExpired = true;
        this.enabled = user.isEnabled(); // ✅ 기존의 true → user 값으로 교체
        this.failedLoginAttempts = user.getFailedLoginAttempts();
        this.lockTime = user.getLockTime();
        this.socialType = user.getSocialType();
        this.socialId = user.getSocialId();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_USER")); // 권한 세팅
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
