package com.gyeongditor.storyfield.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.gyeongditor.storyfield.config.AwsProperties;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.jwt.JwtTokenProvider;
import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;
    private final AwsProperties awsProperties;
    private final JwtTokenProvider jwtTokenProvider;

    // 오디오 허용 MIME 타입
    private static final Set<String> ALLOWED_AUDIO_TYPES = Set.of(
            "audio/wav", "audio/x-wav", "audio/mpeg", "audio/mp3",
            "audio/mp4", "audio/x-mp4", "audio/ogg", "audio/flac", 
            "audio/m4a", "audio/x-m4a", "audio/aac", "audio/webm",
            "application/octet-stream" // 일부 브라우저에서 오디오를 이렇게 인식하는 경우
    );

    // 오디오 파일 확장자 허용 목록
    private static final Set<String> ALLOWED_AUDIO_EXTENSIONS = Set.of(
            ".mp3", ".wav", ".m4a", ".aac", ".ogg", ".flac", ".mp4", ".webm"
    );

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

    // 이미지, 일반 파일 업로드
    public ApiResponseDTO<String> uploadFile(MultipartFile file, String accessToken) throws IOException {
        jwtTokenProvider.validateOrThrow(accessToken);

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        amazonS3.putObject(awsProperties.getBucket(), fileName, file.getInputStream(), metadata);

        return ApiResponseDTO.success(SuccessCode.FILE_200_001, getFileUrl(fileName));
    }

    // 오디오 파일 업로드 (크기 + MIME 타입 검증 포함)
    public ApiResponseDTO<String> uploadAudioFile(MultipartFile file, String accessToken) throws IOException {
        jwtTokenProvider.validateOrThrow(accessToken);

        validateAudioFile(file);

        String fileName = "audio/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        amazonS3.putObject(awsProperties.getBucket(), fileName, file.getInputStream(), metadata);

        return ApiResponseDTO.success(SuccessCode.FILE_200_001, getFileUrl(fileName));
    }

    // 파일 URL 조회
    public ApiResponseDTO<String> getFileUrlResponse(String fileName, String accessToken) {
        jwtTokenProvider.validateOrThrow(accessToken);
        return ApiResponseDTO.success(SuccessCode.FILE_200_003, getFileUrl(fileName));
    }

    // 파일 삭제
    public ApiResponseDTO<Void> deleteFile(String fileName, String accessToken) {
        jwtTokenProvider.validateOrThrow(accessToken);
        amazonS3.deleteObject(awsProperties.getBucket(), fileName);
        return ApiResponseDTO.success(SuccessCode.FILE_204_001, null);
    }

    // S3 URL 생성
    private String getFileUrl(String fileName) {
        return amazonS3.getUrl(awsProperties.getBucket(), fileName).toString();
    }

    // 오디오 파일 검증 (크기 + MIME 타입 + 확장자)
    private void validateAudioFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.FILE_400_001);
        }

        if (file.getSize() > 50 * 1024 * 1024) { // 50MB 제한
            throw new CustomException(ErrorCode.FILE_413_002);
        }

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        
        // 로깅으로 실제 전달되는 값들 확인
        System.out.println("업로드된 파일 정보:");
        System.out.println("- 파일명: " + originalFilename);
        System.out.println("- Content-Type: " + contentType);
        System.out.println("- 파일 크기: " + file.getSize() + " bytes");

        // 파일 확장자 추출
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        // MIME 타입과 확장자 둘 중 하나라도 유효하면 통과
        boolean validMimeType = contentType != null && ALLOWED_AUDIO_TYPES.contains(contentType);
        boolean validExtension = !fileExtension.isEmpty() && ALLOWED_AUDIO_EXTENSIONS.contains(fileExtension);

        if (!validMimeType && !validExtension) {
            System.out.println("❌ 파일 검증 실패:");
            System.out.println("- MIME 타입이 유효하지 않음: " + contentType);
            System.out.println("- 파일 확장자가 유효하지 않음: " + fileExtension);
            System.out.println("- 허용된 MIME 타입: " + ALLOWED_AUDIO_TYPES);
            System.out.println("- 허용된 확장자: " + ALLOWED_AUDIO_EXTENSIONS);
            throw new CustomException(ErrorCode.FILE_400_002);
        }

        System.out.println("✅ 파일 검증 성공:");
        System.out.println("- MIME 타입 유효: " + validMimeType);
        System.out.println("- 확장자 유효: " + validExtension);
    }
}
