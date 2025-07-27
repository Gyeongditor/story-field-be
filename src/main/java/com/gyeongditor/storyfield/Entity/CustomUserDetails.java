package com.gyeongditor.storyfield.Entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final UUID id; // 여기에 UUID 추가
    private final String email;
    private final String password;
    private final String username;
    private final boolean accountNonExpired; // 계정 만료 여부
    private final boolean accountNonLocked; // 계정 잠김 여부
    private final boolean credentialsNonExpired; // 자격 증명 만료 여부
    private final boolean enabled; // 계정 활성화 여부
    private final int failedLoginAttempts;
    private final LocalDateTime lockTime;
    private final Collection<? extends GrantedAuthority> authorities; // 사용자 권한 목록

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
