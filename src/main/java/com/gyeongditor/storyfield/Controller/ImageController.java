package com.gyeongditor.storyfield.Controller;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.service.S3Service;
import com.gyeongditor.storyfield.swagger.api.ImageApi;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ImageController implements ImageApi {

    private final S3Service s3Service;

    @Override
    public ApiResponseDTO<String> getImageUrl(String fileName, HttpServletRequest request) {
        return s3Service.getFileUrlResponse(fileName, request);
    }

    @Override
    public ApiResponseDTO<Void> deleteImage(String fileName, HttpServletRequest request) {
        return s3Service.deleteFile(fileName, request);
    }
}
