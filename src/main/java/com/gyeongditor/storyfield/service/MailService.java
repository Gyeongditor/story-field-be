package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;

public interface MailService {

    ApiResponseDTO<String> sendEmail(String to, String verificationUrl, String subject);
}