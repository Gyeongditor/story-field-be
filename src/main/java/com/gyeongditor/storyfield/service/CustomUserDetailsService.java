package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.Entity.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface CustomUserDetailsService extends UserDetailsService {

    void handleAccountStatus(String email);

    void processFailedLogin(String email);

    void processSuccessfulLogin(String email);

    int getRemainingLoginAttempts(String email);

    CustomUserDetails loadUserByUUID(String uuid);
}