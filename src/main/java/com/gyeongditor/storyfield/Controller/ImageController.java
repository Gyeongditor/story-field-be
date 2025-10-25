package com.gyeongditor.storyfield.Controller;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.service.AuthService;
import com.gyeongditor.storyfield.service.S3Service;
import com.gyeongditor.storyfield.swagger.api.ImageApi;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ImageController implements ImageApi {

    private final S3Service s3Service;
    private final AuthService authService;

    @Override
    public ApiResponseDTO<List<String>> uploadImageFile(List<MultipartFile> files, HttpServletRequest request) {
        return s3Service.uploadImageFile(files, request);
    }

    @Override
    public ApiResponseDTO<String> getImagePresignedUrl(String fileName, HttpServletRequest request) {
        String accessToken = authService.extractAccessToken(request);
        return s3Service.generatePresignedUrl(fileName, accessToken);
    }

    @Override
    public ApiResponseDTO<String> getImageUrl(String fileName, HttpServletRequest request) {
        return s3Service.getFileUrlResponse(fileName, request);
    }

    @Override
    public ApiResponseDTO<Void> deleteImage(String fileName, HttpServletRequest request) {
        return s3Service.deleteFile(fileName, request);
    }
}
