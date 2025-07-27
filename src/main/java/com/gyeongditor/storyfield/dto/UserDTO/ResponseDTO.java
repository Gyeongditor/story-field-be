package com.gyeongditor.storyfield.dto.UserDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseDTO {
    private int statusCode;
    private String message;
}
