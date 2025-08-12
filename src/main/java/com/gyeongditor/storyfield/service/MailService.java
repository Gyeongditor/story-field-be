package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;

    public ApiResponseDTO<String> sendEmail(String to, String verificationUrl, String subject) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText("이메일 인증을 완료하려면 아래 링크를 클릭하세요:\n\n" + verificationUrl);

        javaMailSender.send(message);  // 예외 발생 시 GlobalResponseHandler에서 처리

        log.info("[메일 전송 완료] 대상: {}, 제목: {}", to, subject);
        return ApiResponseDTO.success(SuccessCode.MAIL_200_001, "메일 전송 성공: " + to);
    }
}
