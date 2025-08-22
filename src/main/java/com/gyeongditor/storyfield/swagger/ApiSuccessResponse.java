package com.gyeongditor.storyfield.swagger;

import com.gyeongditor.storyfield.response.SuccessCode;
import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiSuccessResponse {
    SuccessCode[] value();
}