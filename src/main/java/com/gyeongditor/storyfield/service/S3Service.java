package com.gyeongditor.storyfield.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.gyeongditor.storyfield.config.AwsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Service
@Component
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;
    private final AwsProperties awsProperties;

    // url 생성
    public String generatePresignedUrl(String fileName) {
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(awsProperties.getRegion())
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(
                                awsProperties.getAccessKey(),
                                awsProperties.getSecretKey()
                        )))
                .build();

        // URL 유효시간 10분 (600초)
        Date expiration = new Date(System.currentTimeMillis() + 600 * 1000);

        return s3Client.generatePresignedUrl(awsProperties.getBucket(), fileName, expiration).toString();
    }

    // 업로드
    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        amazonS3.putObject(awsProperties.getBucket(), fileName, file.getInputStream(), metadata);

        return getFileUrl(fileName);
    }

    // 파일 URL 조회
    public String getFileUrl(String fileName) {
        return amazonS3.getUrl(awsProperties.getBucket(), fileName).toString();
    }

    // 삭제
    public void deleteFile(String fileName) {
        amazonS3.deleteObject(awsProperties.getBucket(), fileName);
    }


}
