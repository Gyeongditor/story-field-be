package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.Entity.User;
import com.gyeongditor.storyfield.oauth.user.OAuth2UserInfo;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface CustomOAuth2UserService extends OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    User getUserByOAuth2UserInfo(OAuth2UserInfo userInfo, String registrationId);
}