package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.response.SuccessCode;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;

    /**
     * 인증 이메일 전송
     */
    public ApiResponseDTO<String> sendEmail(String to, String verificationUrl, String subject) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText("이메일 인증을 완료하려면 아래 링크를 클릭하세요:\n\n" + verificationUrl);

            javaMailSender.send(message);

            log.info("[메일 전송 완료] 대상: {}, 제목: {}", to, subject);

            return ApiResponseDTO.success(SuccessCode.MAIL_200_001, "메일 전송 성공: " + to);

        } catch (MailException e) {
            log.error("[메일 전송 실패] 대상: {}, 오류: {}", to, e.getMessage(), e);
            throw new CustomException(ErrorCode.MAIL_500_001, "메일 전송에 실패했습니다. 대상: " + to);
        }
    }
}
