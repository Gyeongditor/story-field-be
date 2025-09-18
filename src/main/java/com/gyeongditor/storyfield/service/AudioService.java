package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

public interface AudioService {

    ApiResponseDTO<String> uploadAudio(MultipartFile file, HttpServletRequest request);

    ApiResponseDTO<String> getAudioUrl(String fileName, HttpServletRequest request);

    ApiResponseDTO<String> generateDownloadPresignedUrl(String keyOrFileName, HttpServletRequest request);

    ApiResponseDTO<Void> deleteAudio(String fileName, HttpServletRequest request);
}
