package com.gyeongditor.storyfield.Entity;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
public class CustomOAuth2User implements OAuth2User {

    private final OAuth2User oAuth2User;
    private final String socialType;
    private final String id;
    private final String name;
    private final String email; // Google/Naver만 필수, Kakao는 null 가능

    public CustomOAuth2User(OAuth2User oAuth2User, String socialType) {
        this.oAuth2User = oAuth2User;
        this.socialType = socialType;
        this.id = extractId(oAuth2User, socialType);
        this.name = extractName(oAuth2User, socialType);
        this.email = extractEmail(oAuth2User, socialType);
    }

    private String extractId(OAuth2User oAuth2User, String socialType) {
        switch (socialType) {
            case "google":
                return (String) oAuth2User.getAttribute("sub");
            case "kakao":
                Object id = oAuth2User.getAttribute("id");
                return id != null ? id.toString() : null;
            case "naver":
                Map<String, Object> response = (Map<String, Object>) oAuth2User.getAttribute("response");
                return (String) response.get("id");
            default:
                throw new IllegalArgumentException("Unsupported social type: " + socialType);
        }
    }

    private String extractName(OAuth2User oAuth2User, String socialType) {
        switch (socialType) {
            case "google":
                return (String) oAuth2User.getAttribute("name");
            case "kakao":
                Map<String, Object> account = (Map<String, Object>) oAuth2User.getAttribute("kakao_account");
                Map<String, Object> profile = (Map<String, Object>) account.get("profile");
                return (String) profile.get("nickname");
            case "naver":
                Map<String, Object> response = (Map<String, Object>) oAuth2User.getAttribute("response");
                return (String) response.get("name");
            default:
                throw new IllegalArgumentException("Unsupported social type: " + socialType);
        }
    }

    private String extractEmail(OAuth2User oAuth2User, String socialType) {
        switch (socialType) {
            case "google":
                return (String) oAuth2User.getAttribute("email");
            case "kakao":
                // Kakao는 이메일 항목을 필수로 요청하지 않을 수 있음 → null 허용
                Map<String, Object> account = (Map<String, Object>) oAuth2User.getAttribute("kakao_account");
                return account != null ? (String) account.get("email") : null;
            case "naver":
                Map<String, Object> response = (Map<String, Object>) oAuth2User.getAttribute("response");
                return (String) response.get("email");
            default:
                return null;
        }
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oAuth2User.getAuthorities();
    }

    @Override
    public String getName() {
        return this.id;
    }
}
