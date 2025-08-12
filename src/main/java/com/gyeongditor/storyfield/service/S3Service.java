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
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;
    private final AwsProperties awsProperties;
    private final JwtTokenProvider jwtTokenProvider;

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

    public ApiResponseDTO<String> uploadFile(MultipartFile file, String accessToken) throws IOException {
        jwtTokenProvider.validateOrThrow(accessToken);

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        amazonS3.putObject(awsProperties.getBucket(), fileName, file.getInputStream(), metadata);

        return ApiResponseDTO.success(SuccessCode.FILE_200_001, getFileUrl(fileName));
    }

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
