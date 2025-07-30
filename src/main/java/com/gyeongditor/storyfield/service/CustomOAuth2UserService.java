package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.Entity.CustomOAuth2User;
import com.gyeongditor.storyfield.Entity.User;
import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.oauth.user.OAuth2UserInfo;
import com.gyeongditor.storyfield.oauth.user.OAuth2UserInfoFactory;
import com.gyeongditor.storyfield.repository.UserRepository;
import com.gyeongditor.storyfield.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        try {
            // Provider ID (Google, Kakao 등)
            String registrationId = userRequest.getClientRegistration().getRegistrationId();

            // 사용자 정보 가져오기
            OAuth2User oauth2User = getOAuth2User(userRequest);

            // OAuth2UserInfo 추출
            OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oauth2User.getAttributes());
            if (userInfo == null) {
                throw new CustomException(ErrorCode.AUTH_401_008, "지원하지 않는 OAuth2 Provider: " + registrationId);
            }

            // 사용자 저장/업데이트
            saveOrUpdateUser(userInfo, registrationId);

            return new CustomOAuth2User(oauth2User, registrationId);

        } catch (CustomException e) {
            throw e; // GlobalResponseHandler에서 처리
        } catch (Exception e) {
            log.error("[OAuth2 인증 오류] {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.AUTH_500_001, "OAuth2 사용자 정보 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * DefaultOAuth2UserService 사용하여 사용자 정보 가져오기
     */
    private OAuth2User getOAuth2User(OAuth2UserRequest userRequest) {
        try {
            return new DefaultOAuth2UserService().loadUser(userRequest);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.AUTH_401_007, "OAuth2 사용자 정보를 가져올 수 없습니다.");
        }
    }

    /**
     * 기존 사용자 업데이트 또는 신규 사용자 생성
     */
    private User saveOrUpdateUser(OAuth2UserInfo userInfo, String registrationId) {
        User user = userRepository.findBySocialId(userInfo.getId())
                .orElseGet(() -> createNewUser(userInfo, registrationId));

        user.updateOAuthUser(userInfo.getName(), userInfo.getEmail(), registrationId, userInfo.getId());

        return userRepository.save(user);
    }

    /**
     * 신규 사용자 생성
     */
    private User createNewUser(OAuth2UserInfo userInfo, String registrationId) {
        return User.builder()
                .username(userInfo.getName())
                .email(userInfo.getEmail())
                .socialType(registrationId)
                .socialId(userInfo.getId())
                .password("") // OAuth2 사용자는 비밀번호 없음
                .enabled(true)
                .build();
    }

    /**
     * 외부에서 OAuth2UserInfo로 사용자 가져오기
     */
    public User getUserByOAuth2UserInfo(OAuth2UserInfo userInfo, String registrationId) {
        return userRepository.findBySocialId(userInfo.getId())
                .orElseGet(() -> createNewUser(userInfo, registrationId));
    }
}
