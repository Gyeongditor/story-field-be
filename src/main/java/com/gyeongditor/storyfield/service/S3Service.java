package com.gyeongditor.storyfield.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.gyeongditor.storyfield.config.AwsProperties;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.jwt.JwtTokenProvider;
import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.response.SuccessCode;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;
    private final AwsProperties awsProperties;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;
    @Value("${aws.s3.bucket}") private String bucket;

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
        jwtTokenProvider.validateOrThrow(accessToken);

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        upload(file, fileName); // 내부 private upload 호출

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

    // 오디오 파일 업로드 (크기 + MIME 타입 검증 포함)
    public ApiResponseDTO<String> uploadAudioFile(MultipartFile file, String accessToken) {
        jwtTokenProvider.validateOrThrow(accessToken);

        validateAudioFile(file);

        try {
            String fileName = "audio/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            amazonS3.putObject(awsProperties.getBucket(), fileName, file.getInputStream(), metadata);

            // Audio 전용 성공코드 사용
            return ApiResponseDTO.success(SuccessCode.AUDIO_200_001, getFileUrl(fileName));

        } catch (IOException e) {
            throw new CustomException(ErrorCode.AUDIO_500_001); // Audio 전용 에러코드로 변경
        } catch (Exception e) {
            throw new CustomException(ErrorCode.AUDIO_500_001); // Audio 전용 에러코드로 변경
        }
    }


    // 단순 URL 조회
    public ApiResponseDTO<String> getFileUrlResponse(String fileName, HttpServletRequest request) {
        String accessToken = authService.extractAccessToken(request);
        jwtTokenProvider.validateOrThrow(accessToken);
        return ApiResponseDTO.success(SuccessCode.FILE_200_003, getFileUrl(fileName));
    }

    // 파일 삭제
    public ApiResponseDTO<Void> deleteFile(String fileName, HttpServletRequest request) {
        String accessToken = authService.extractAccessToken(request);
        jwtTokenProvider.validateOrThrow(accessToken);
        amazonS3.deleteObject(awsProperties.getBucket(), fileName);
        return ApiResponseDTO.success(SuccessCode.FILE_204_001, null);
    }

    // S3 URL 생성
    private String getFileUrl(String fileName) {
        return amazonS3.getUrl(awsProperties.getBucket(), fileName).toString();
    }

    // 키 정규화 (파일명 보정 + 보안)
    private String normalizeAudioKey(String fileName) {
        String decoded = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
        // 역슬래시 → 슬래시, 경로 역참조 차단
        decoded = decoded.replace("\\", "/");
        if (decoded.contains("..")) {
            throw new CustomException(ErrorCode.AUDIO_400_002); // "잘못된 파일 경로"
        }
        // 앞에 audio/ 없으면 붙여줌
        return decoded.startsWith("audio/") ? decoded : "audio/" + decoded;
    }

    // 오디오 파일 존재 확인
    private boolean audioExists(String key) {
        return amazonS3.doesObjectExist(awsProperties.getBucket(), key);
    }

    // 오디오 URL 조회 (존재 확인 포함)
    public ApiResponseDTO<String> getAudioFileUrl(String fileName, String accessToken) {
        jwtTokenProvider.validateOrThrow(accessToken);
        try {
            String key = normalizeAudioKey(fileName);
            if (!audioExists(key)) {
                throw new CustomException(ErrorCode.AUDIO_404_001); // "오디오 파일을 찾을 수 없음"
            }
            return ApiResponseDTO.success(SuccessCode.AUDIO_200_003, getFileUrl(key));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.AUDIO_500_003); // "오디오 URL 조회 실패"
        }
    }

    // 오디오 삭제 (존재 확인 포함)
    public ApiResponseDTO<Void> deleteAudioFile(String fileName, String accessToken) {
        jwtTokenProvider.validateOrThrow(accessToken);
        try {
            String key = normalizeAudioKey(fileName);

            // 1) 삭제 전 존재 여부 확인
            if (!audioExists(key)) {
                throw new CustomException(ErrorCode.AUDIO_404_001);
            }

            // 2) 삭제
            amazonS3.deleteObject(awsProperties.getBucket(), key);

            // 3) 삭제 확인
            if (audioExists(key)) {
                throw new CustomException(ErrorCode.AUDIO_500_002); // "오디오 삭제 실패"
            }

            return ApiResponseDTO.success(SuccessCode.AUDIO_204_001, null);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.AUDIO_500_002);
        }
    }

    // 오디오 파일 검증 (크기 + MIME 타입 + 확장자)
    private void validateAudioFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.AUDIO_400_001); // Audio 전용 에러코드로 변경
        }

        if (file.getSize() > 50 * 1024 * 1024) { // 50MB 제한
            throw new CustomException(ErrorCode.AUDIO_413_001); // Audio 전용 에러코드로 변경
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
            System.out.println(" 파일 검증 실패:");
            System.out.println("- MIME 타입이 유효하지 않음: " + contentType);
            System.out.println("- 파일 확장자가 유효하지 않음: " + fileExtension);
            System.out.println("- 허용된 MIME 타입: " + ALLOWED_AUDIO_TYPES);
            System.out.println("- 허용된 확장자: " + ALLOWED_AUDIO_EXTENSIONS);
            throw new CustomException(ErrorCode.AUDIO_400_002); // Audio 전용 에러코드로 변경
        }

        System.out.println(" 파일 검증 성공:");
        System.out.println("- MIME 타입 유효: " + validMimeType);
        System.out.println("- 확장자 유효: " + validExtension);
    }

    // 다운로드용 Presigned URL (GET) - 존재 확인 후 URL 반환
    public ApiResponseDTO<String> generateDownloadPresignedUrl(String keyOrFileName, String accessToken) {
        jwtTokenProvider.validateOrThrow(accessToken);
        try {
            String key = normalizeAudioKey(keyOrFileName);

            // 존재 확인: 없으면 404
            if (!audioExists(key)) {
                throw new CustomException(ErrorCode.AUDIO_404_001);
            }

            Date expiration = new Date(System.currentTimeMillis() + 10 * 60 * 1000); // 10분
            GeneratePresignedUrlRequest req =
                    new GeneratePresignedUrlRequest(awsProperties.getBucket(), key)
                            .withMethod(HttpMethod.GET)
                            .withExpiration(expiration);

            String url = amazonS3.generatePresignedUrl(req).toString();
            // 다운로드 presign도 "URL 조회 성공" 의미의 성공코드 사용
            return ApiResponseDTO.success(SuccessCode.AUDIO_200_003, url);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.AUDIO_500_004); // Presigned URL 생성 실패
        }
    }

    public String uploadBytes(byte[] bytes, String objectKey, String contentType) throws IOException {
        // accessToken 유효성 검증(필요시): jwtTokenProvider.validateOrThrow(accessToken);
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType(contentType);
        meta.setContentLength(bytes.length);
        try (InputStream in = new ByteArrayInputStream(bytes)) {
            amazonS3.putObject(new PutObjectRequest(bucket, objectKey, in, meta));
        }
        return objectKey; // DB 저장용 key 반환
    }
}
