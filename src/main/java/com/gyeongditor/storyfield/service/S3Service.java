package com.gyeongditor.storyfield.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.gyeongditor.storyfield.config.AwsProperties;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.response.ErrorCode;
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

    public ApiResponseDTO<String> generatePresignedUrl(String fileName) {
        try {
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
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_500_002, "Presigned URL 생성에 실패했습니다.");
        }
    }

    public ApiResponseDTO<String> uploadFile(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            amazonS3.putObject(awsProperties.getBucket(), fileName, file.getInputStream(), metadata);

            return ApiResponseDTO.success(SuccessCode.FILE_200_001, getFileUrl(fileName));
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_500_001, "파일 업로드 중 오류가 발생했습니다.");
        }
    }

    public ApiResponseDTO<String> getFileUrlResponse(String fileName) {
        try {
            return ApiResponseDTO.success(SuccessCode.FILE_200_003, getFileUrl(fileName));
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_500_003, "파일 URL 조회에 실패했습니다.");
        }
    }

    private String getFileUrl(String fileName) {
        return amazonS3.getUrl(awsProperties.getBucket(), fileName).toString();
    }

    public ApiResponseDTO<Void> deleteFile(String fileName) {
        try {
            amazonS3.deleteObject(awsProperties.getBucket(), fileName);
            return ApiResponseDTO.success(SuccessCode.FILE_204_001, null);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_500_004, "파일 삭제 중 오류가 발생했습니다.");
        }
    }
}

