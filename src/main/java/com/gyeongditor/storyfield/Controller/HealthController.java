package com.gyeongditor.storyfield.Controller;

import com.gyeongditor.storyfield.config.FastApiClient;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.response.SuccessCode;
import com.gyeongditor.storyfield.swagger.api.HealthApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequiredArgsConstructor
public class HealthController implements HealthApi {

    private final FastApiClient fastApiClient;

    @Override
    public ApiResponseDTO<String> ping() {
        String fastApiResponse = fastApiClient.ping();
        return ApiResponseDTO.success(SuccessCode.SUCCESS_200_001, fastApiResponse);
    }
}
