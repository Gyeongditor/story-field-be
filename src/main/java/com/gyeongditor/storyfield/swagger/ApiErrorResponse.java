package com.gyeongditor.storyfield.swagger;

import com.gyeongditor.storyfield.response.ErrorCode;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorResponse {
    ErrorCode[] value(); // 적용할 ErrorCode 목록
}
