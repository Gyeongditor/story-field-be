package com.gyeongditor.storyfield.service.impl;

import com.gyeongditor.storyfield.Entity.CustomOAuth2User;
import com.gyeongditor.storyfield.Entity.User;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.oauth.user.OAuth2UserInfo;
import com.gyeongditor.storyfield.oauth.user.OAuth2UserInfoFactory;
import com.gyeongditor.storyfield.repository.UserRepository;
import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.response.SuccessCode;
import com.gyeongditor.storyfield.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserServiceImpl implements CustomOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oauth2User.getAttributes());
        if (userInfo == null) {
            throw new CustomException(ErrorCode.AUTH_401_008, "지원하지 않는 OAuth2 Provider: " + registrationId);
        }

        boolean isNewUser = saveOrUpdateUser(userInfo, registrationId);

        SuccessCode successCode = isNewUser ? SuccessCode.OAUTH2_201_001 : SuccessCode.OAUTH2_200_001;
        log.info(ApiResponseDTO.success(successCode, userInfo.getEmail()).toString());

        return new CustomOAuth2User(oauth2User, registrationId);
    }

    private boolean saveOrUpdateUser(OAuth2UserInfo userInfo, String registrationId) {
        return userRepository.findBySocialId(userInfo.getId())
                .map(user -> {
                    user.updateOAuthUser(userInfo.getName(), userInfo.getEmail(), registrationId, userInfo.getId());
                    userRepository.save(user);
                    return false;
                })
                .orElseGet(() -> {
                    userRepository.save(createNewUser(userInfo, registrationId));
                    return true;
                });
    }

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

    @Override
    public User getUserByOAuth2UserInfo(OAuth2UserInfo userInfo, String registrationId) {
        return userRepository.findBySocialId(userInfo.getId())
                .orElseGet(() -> createNewUser(userInfo, registrationId));
    }
}