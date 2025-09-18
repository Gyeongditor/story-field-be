package com.gyeongditor.storyfield.service.impl;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.gyeongditor.storyfield.config.AwsProperties;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.jwt.JwtTokenProvider;
import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.response.SuccessCode;
import com.gyeongditor.storyfield.service.AuthService;
import com.gyeongditor.storyfield.service.S3Service;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final AmazonS3 amazonS3;
    private final AwsProperties awsProperties;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;

    @Value("${aws.s3.bucket}")
    private String bucket;

    private static final Set<String> ALLOWED_AUDIO_TYPES = Set.of(
            "audio/wav", "audio/x-wav", "audio/mpeg", "audio/mp3",
            "audio/mp4", "audio/x-mp4", "audio/ogg", "audio/flac",
            "audio/m4a", "audio/x-m4a", "audio/aac", "audio/webm",
            "application/octet-stream"
    );

    private static final Set<String> ALLOWED_AUDIO_EXTENSIONS = Set.of(
            ".mp3", ".wav", ".m4a", ".aac", ".ogg", ".flac", ".mp4", ".webm"
    );

    @Override
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

    @Override
    public List<String> uploadFiles(List<MultipartFile> files, String accessToken) throws IOException {
        jwtTokenProvider.validateOrThrow(accessToken);

        List<String> uploadedFileNames = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                upload(file, fileName);
                uploadedFileNames.add(fileName);
            }
        }

        return uploadedFileNames;
    }

    @Override
    public String uploadThumbnailFile(MultipartFile file, String accessToken) throws IOException {
        jwtTokenProvider.validateOrThrow(accessToken);

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        upload(file, fileName);

        return fileName;
    }

    private void upload(MultipartFile file, String fileName) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        amazonS3.putObject(
                awsProperties.getBucket(),
                fileName,
                file.getInputStream(),
                metadata
        );
    }

    @Override
    public ApiResponseDTO<String> uploadAudioFile(MultipartFile file, String accessToken) {
        jwtTokenProvider.validateOrThrow(accessToken);

        validateAudioFile(file);

        try {
            String fileName = "audio/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            amazonS3.putObject(awsProperties.getBucket(), fileName, file.getInputStream(), metadata);

            return ApiResponseDTO.success(SuccessCode.AUDIO_200_001, getFileUrl(fileName));

        } catch (IOException e) {
            throw new CustomException(ErrorCode.AUDIO_500_001);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.AUDIO_500_001);
        }
    }

    @Override
    public ApiResponseDTO<String> getFileUrlResponse(String fileName, HttpServletRequest request) {
        String accessToken = authService.extractAccessToken(request);
        jwtTokenProvider.validateOrThrow(accessToken);
        return ApiResponseDTO.success(SuccessCode.FILE_200_003, getFileUrl(fileName));
    }

    @Override
    public ApiResponseDTO<Void> deleteFile(String fileName, HttpServletRequest request) {
        String accessToken = authService.extractAccessToken(request);
        jwtTokenProvider.validateOrThrow(accessToken);
        amazonS3.deleteObject(awsProperties.getBucket(), fileName);
        return ApiResponseDTO.success(SuccessCode.FILE_204_001, null);
    }

    private String getFileUrl(String fileName) {
        return amazonS3.getUrl(awsProperties.getBucket(), fileName).toString();
    }

    private String normalizeAudioKey(String fileName) {
        String decoded = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
        decoded = decoded.replace("\\", "/");
        if (decoded.contains("..")) {
            throw new CustomException(ErrorCode.AUDIO_400_002);
        }
        return decoded.startsWith("audio/") ? decoded : "audio/" + decoded;
    }

    private boolean audioExists(String key) {
        return amazonS3.doesObjectExist(awsProperties.getBucket(), key);
    }

    @Override
    public ApiResponseDTO<String> getAudioFileUrl(String fileName, String accessToken) {
        jwtTokenProvider.validateOrThrow(accessToken);
        try {
            String key = normalizeAudioKey(fileName);
            if (!audioExists(key)) {
                throw new CustomException(ErrorCode.AUDIO_404_001);
            }
            return ApiResponseDTO.success(SuccessCode.AUDIO_200_003, getFileUrl(key));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.AUDIO_500_003);
        }
    }

    @Override
    public ApiResponseDTO<Void> deleteAudioFile(String fileName, String accessToken) {
        jwtTokenProvider.validateOrThrow(accessToken);
        try {
            String key = normalizeAudioKey(fileName);

            if (!audioExists(key)) {
                throw new CustomException(ErrorCode.AUDIO_404_001);
            }

            amazonS3.deleteObject(awsProperties.getBucket(), key);

            if (audioExists(key)) {
                throw new CustomException(ErrorCode.AUDIO_500_002);
            }

            return ApiResponseDTO.success(SuccessCode.AUDIO_204_001, null);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.AUDIO_500_002);
        }
    }

    @Override
    public ApiResponseDTO<String> generateDownloadPresignedUrl(String keyOrFileName, String accessToken) {
        jwtTokenProvider.validateOrThrow(accessToken);
        try {
            String key = normalizeAudioKey(keyOrFileName);

            if (!audioExists(key)) {
                throw new CustomException(ErrorCode.AUDIO_404_001);
            }

            Date expiration = new Date(System.currentTimeMillis() + 10 * 60 * 1000);
            GeneratePresignedUrlRequest req =
                    new GeneratePresignedUrlRequest(awsProperties.getBucket(), key)
                            .withMethod(HttpMethod.GET)
                            .withExpiration(expiration);

            String url = amazonS3.generatePresignedUrl(req).toString();
            return ApiResponseDTO.success(SuccessCode.AUDIO_200_003, url);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.AUDIO_500_004);
        }
    }

    @Override
    public String uploadBytes(byte[] bytes, String objectKey, String contentType) throws IOException {
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType(contentType);
        meta.setContentLength(bytes.length);
        try (InputStream in = new ByteArrayInputStream(bytes)) {
            amazonS3.putObject(new PutObjectRequest(bucket, objectKey, in, meta));
        }
        return objectKey;
    }

    private void validateAudioFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.AUDIO_400_001);
        }

        if (file.getSize() > 50 * 1024 * 1024) {
            throw new CustomException(ErrorCode.AUDIO_413_001);
        }

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        boolean validMimeType = contentType != null && ALLOWED_AUDIO_TYPES.contains(contentType);
        boolean validExtension = !fileExtension.isEmpty() && ALLOWED_AUDIO_EXTENSIONS.contains(fileExtension);

        if (!validMimeType && !validExtension) {
            throw new CustomException(ErrorCode.AUDIO_400_002);
        }
    }
}