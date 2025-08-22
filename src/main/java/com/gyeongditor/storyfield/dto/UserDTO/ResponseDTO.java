package com.gyeongditor.storyfield.dto.UserDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor  // Jackson이 필요로 함
@AllArgsConstructor // 생성자 기반 주입도 가능
@Schema(description = "응답 DTO")
public class ResponseDTO {
    private int statusCode;
    private String message;
}
