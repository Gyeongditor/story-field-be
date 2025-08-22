package com.gyeongditor.storyfield.Controller;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.response.SuccessCode;
import com.gyeongditor.storyfield.swagger.HealthApi;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController implements HealthApi {

    @Override
    public ApiResponseDTO<String> ping() {
        return ApiResponseDTO.success(SuccessCode.SUCCESS_200_001, "OK");
    }
}
