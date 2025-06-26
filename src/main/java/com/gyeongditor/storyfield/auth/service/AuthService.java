package com.gyeongditor.storyfield.auth.service;

import com.gyeongditor.storyfield.Entity.User;
import com.gyeongditor.storyfield.Repository.UserRepository;
import com.gyeongditor.storyfield.auth.util.JwtTokenProvider;
import com.gyeongditor.storyfield.dto.LoginDTO.LoginRequestDTO;
import com.gyeongditor.storyfield.dto.LoginDTO.LoginResponseDTO;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        User user = userRepository.findByLoginId(loginRequestDTO.getLoginId())
                .orElseThrow(() -> new UsernameNotFoundException("사용자가 존재하지 않습니다."));

        if (!passwordEncoder.matches(loginRequestDTO.getUserPw(), user.getUserPw())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createToken(user.getUserId());
        long expiration = jwtTokenProvider.getExpiration(accessToken);

        // Redis에 토큰 저장 (블랙리스트 체크 용도)
        redisTemplate.opsForValue()
                .set("access:" + user.getUserId(), accessToken, Duration.ofMillis(expiration));

        return new LoginResponseDTO(accessToken);
    }

    public void logout(String userId) {
        String key = "access:" + userId;
        Boolean deleted = redisTemplate.delete(key);
        if (deleted == null || !deleted) {
            throw new IllegalStateException("로그아웃 실패: Redis에 저장된 토큰이 없습니다.");
        }
    }
}
