package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface S3Service {

    ApiResponseDTO<String> generatePresignedUrl(String fileName, String accessToken);

    List<String> uploadFiles(List<MultipartFile> files, String accessToken) throws IOException;

    String uploadThumbnailFile(MultipartFile file, String accessToken) throws IOException;

    ApiResponseDTO<String> uploadAudioFile(MultipartFile file, String accessToken);

    ApiResponseDTO<String> getFileUrlResponse(String fileName, HttpServletRequest request);

    ApiResponseDTO<Void> deleteFile(String fileName, HttpServletRequest request);

    ApiResponseDTO<String> getAudioFileUrl(String fileName, String accessToken);

    ApiResponseDTO<Void> deleteAudioFile(String fileName, String accessToken);

    ApiResponseDTO<String> generateDownloadPresignedUrl(String keyOrFileName, String accessToken);

    String uploadBytes(byte[] bytes, String objectKey, String contentType) throws IOException;
}