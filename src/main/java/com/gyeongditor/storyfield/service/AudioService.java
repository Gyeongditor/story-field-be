package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AudioService {

    private final S3Service s3Service;

    public ApiResponseDTO<String> uploadAudio(MultipartFile file, String accessToken) {
        return s3Service.uploadAudioFile(file, accessToken);
    }

    public ApiResponseDTO<String> getAudioUrl(String fileName, String accessToken) {
        return s3Service.getAudioFileUrl(fileName, accessToken);
    }

    // 다운로드용 presign (URL 문자열만 반환)
    public ApiResponseDTO<String> generateDownloadPresignedUrl(String keyOrFileName, String accessToken) {
        return s3Service.generateDownloadPresignedUrl(keyOrFileName, accessToken);
    }

    public ApiResponseDTO<Void> deleteAudio(String fileName, String accessToken) {
        return s3Service.deleteAudioFile(fileName, accessToken);
    }
}
