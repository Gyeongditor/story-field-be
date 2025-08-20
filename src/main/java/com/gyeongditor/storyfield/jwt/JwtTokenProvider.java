package com.gyeongditor.storyfield.jwt;

import com.gyeongditor.storyfield.Entity.CustomUserDetails;
import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.repository.JwtTokenRedisRepository;
import com.gyeongditor.storyfield.response.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
@Getter
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.token-validity-in-seconds}")
    private long accessTokenValiditySeconds;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    public long refreshTokenValiditySeconds;

    private final JwtTokenRedisRepository jwtTokenRedisRepository;
    private final UserDetailsService userDetailsService;

    /**
     * AccessToken 생성
     */
    public String createToken(Authentication authentication) {
        return generateToken(authentication, accessTokenValiditySeconds);
    }

    /**
     * RefreshToken 생성
     */
    public String createRefreshToken(Authentication authentication) {
        return generateToken(authentication, refreshTokenValiditySeconds);
    }

    private String generateToken(Authentication authentication, long validitySeconds) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Claims claims = Jwts.claims().setSubject(userDetails.getEmail()); // sub = email
        claims.put("userUUID", userDetails.getUserId().toString());       // 🔑 uuid 별도 claim 추가

        Date now = new Date();
        Date expiry = new Date(now.getTime() + validitySeconds * 1000);

        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .setClaims(claims)
                .setId(jti)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }


    /**
     * 토큰 유효성 검사 실패 시 예외 발생
     */
    public void validateOrThrow(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new CustomException(ErrorCode.AUTH_401_010, "인증 토큰이 없습니다.");
        }
        
        try {
            Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
            
            // 토큰 만료 검사
            if (claims.getExpiration().before(new Date())) {
                throw new CustomException(ErrorCode.AUTH_401_011, "인증 토큰이 만료되었습니다.");
            }
            
            // 블랙리스트 검사
            String jti = claims.getId();
            if (jti != null && jwtTokenRedisRepository.isTokenBlacklisted(jti)) {
                throw new CustomException(ErrorCode.AUTH_401_012, "유효하지 않은 인증 토큰입니다.");
            }
            
        } catch (CustomException e) {
            throw e; // CustomException은 그대로 재던지기
        } catch (Exception e) {
            throw new CustomException(ErrorCode.AUTH_401_012, "유효하지 않은 액세스 토큰입니다.");
        }
    }

    public void validateRefreshOrThrow(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new CustomException(ErrorCode.AUTH_401_003, "RefreshToken이 없습니다.");
        }
        
        try {
            Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(refreshToken).getBody();
            
            // 토큰 만료 검사
            if (claims.getExpiration().before(new Date())) {
                throw new CustomException(ErrorCode.AUTH_401_005, "RefreshToken이 만료되었습니다.");
            }
            
            // 블랙리스트 검사
            String jti = claims.getId();
            if (jti != null && jwtTokenRedisRepository.isTokenBlacklisted(jti)) {
                throw new CustomException(ErrorCode.AUTH_401_012, "유효하지 않은 RefreshToken입니다.");
            }
            
        } catch (CustomException e) {
            throw e; // CustomException은 그대로 재던지기
        } catch (Exception e) {
            throw new CustomException(ErrorCode.AUTH_401_005, "유효하지 않은 리프레시 토큰입니다.");
        }
    }

    /**
     * Email 추출
     */
    public String getEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    public String resolveRefreshToken(HttpServletRequest request) {
        return request.getHeader("Refresh-Token");
    }

    /**
     * RefreshToken → AccessToken 재발급
     */
    public String createTokenFromRefreshToken(String refreshToken) {
        validateRefreshOrThrow(refreshToken);

        String email = getEmail(refreshToken);
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);

        return generateToken(
                new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities()),
                accessTokenValiditySeconds
        );
    }

    /**
     * 인증 객체 추출
     */
    public Authentication getAuthentication(String token) {
        String email = getEmail(token);
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    /**
     * 토큰 파싱
     */
    public Claims parseClaims(String token) {
        try {
            return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.AUTH_401_004, "토큰 파싱에 실패했습니다.");
        }
    }

    /**
     * AccessToken & RefreshToken 무효화 처리
     */
    public void invalidateTokensOrThrow(String accessToken, String refreshToken) {
        // 1. RefreshToken 삭제
        Claims refreshClaims = parseClaims(refreshToken);
        String userUUID = (String) refreshClaims.get("userUUID"); // ✅ 여기서 uuid 추출
        if (userUUID == null) {
            throw new CustomException(ErrorCode.AUTH_401_007, "RefreshToken에 userUUID 클레임이 없습니다.");
        }
        jwtTokenRedisRepository.deleteRefreshToken(userUUID);

        // 2. AccessToken 블랙리스트 처리 (30분 TTL)
        Claims accessClaims = parseClaims(accessToken);
        String accessJti = accessClaims.getId();
        if (accessJti == null) {
            throw new CustomException(ErrorCode.AUTH_401_007, "AccessToken에 jti 클레임이 없습니다.");
        }

        long ttlSeconds = 30 * 60;
        boolean success = jwtTokenRedisRepository.addTokenToBlacklist(accessJti, ttlSeconds);
        if (!success) {
            throw new CustomException(ErrorCode.SERVER_500_001, "AccessToken 블랙리스트 등록 실패");
        }
    }

    public boolean isRefreshTokenBlacklisted(String refreshToken) {
        String tokenId = parseClaims(refreshToken).getId();
        return jwtTokenRedisRepository.isTokenBlacklisted(tokenId);
    }
}
