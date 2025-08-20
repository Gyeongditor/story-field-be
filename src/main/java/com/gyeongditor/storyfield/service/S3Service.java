package com.gyeongditor.storyfield.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.gyeongditor.storyfield.config.AwsProperties;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.jwt.JwtTokenProvider;
import com.gyeongditor.storyfield.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;
    private final AwsProperties awsProperties;
    private final JwtTokenProvider jwtTokenProvider;

    // Presigned URL 발급
    public ApiResponseDTO<String> generatePresignedUrl(String fileName, String accessToken) {
        jwtTokenProvider.validateOrThrow(accessToken);

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(awsProperties.getRegion())
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(
                                awsProperties.getAccessKey(),
                                awsProperties.getSecretKey()
                        ))).build();

        Date expiration = new Date(System.currentTimeMillis() + 600 * 1000);
        String presignedUrl = s3Client.generatePresignedUrl(awsProperties.getBucket(), fileName, expiration).toString();

        return ApiResponseDTO.success(SuccessCode.FILE_200_002, presignedUrl);
    }

    // 단순 파일 업로드 (ApiResponse 반환)
    public List<String> uploadFiles(List<MultipartFile> files, String accessToken) throws IOException {
        jwtTokenProvider.validateOrThrow(accessToken);

        List<String> uploadedFileNames = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                upload(file, fileName); // 내부 private upload 호출
                uploadedFileNames.add(fileName);
            }
        }

        return uploadedFileNames;
    }

    public String uploadThumbnailFile(MultipartFile file, String accessToken) throws IOException {
        // 1. 토큰 검증
        jwtTokenProvider.validateOrThrow(accessToken);

        // 2. 파일명 생성 (UUID 붙이기)
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        // 3. 업로드
        upload(file, fileName); // 내부 private upload 호출

        // 4. 업로드된 파일 URL 반환

        return fileName;
    }


    // 내부 공통 업로드 로직
    private void upload(MultipartFile file, String fileName) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        amazonS3.putObject(
                awsProperties.getBucket(),
                fileName,
                file.getInputStream(), // IOException 발생 가능
                metadata
        );
    }
    // 단순 URL 조회
    public ApiResponseDTO<String> getFileUrlResponse(String fileName, String accessToken) {
        jwtTokenProvider.validateOrThrow(accessToken);
        return ApiResponseDTO.success(SuccessCode.FILE_200_003, getFileUrl(fileName));
    }

    public ApiResponseDTO<Void> deleteFile(String fileName, String accessToken) {
        jwtTokenProvider.validateOrThrow(accessToken);
        amazonS3.deleteObject(awsProperties.getBucket(), fileName);
        return ApiResponseDTO.success(SuccessCode.FILE_204_001, null);
    }

    private String getFileUrl(String fileName) {
        return amazonS3.getUrl(awsProperties.getBucket(), fileName).toString();
    }
}
