package com.gyeongditor.storyfield.handler.mapper;

import com.gyeongditor.storyfield.response.ErrorCode;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Component;

@Component
public class MailErrorMapper {
    public MappedError map(MailException ex) {
        return new MappedError(ErrorCode.MAIL_500_001, "메일 전송 중 오류가 발생했습니다.");
    }
}
