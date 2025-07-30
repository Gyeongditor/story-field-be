package com.gyeongditor.storyfield.Controller;

import com.gyeongditor.storyfield.config.AwsProperties;
import com.gyeongditor.storyfield.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {

    private final S3Service s3Service;
    private final AwsProperties awsProperties;

    @GetMapping("/image-url")
    public ResponseEntity<String> getPresignedUrl(@RequestParam String fileName) {
        String url = s3Service.generatePresignedUrl(fileName);
        return ResponseEntity.ok(url);
    }

    // 업로드
    @PostMapping
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        String fileName = s3Service.uploadFile(file);
        String url = getS3Url(fileName);
        return ResponseEntity.ok(url);
    }

    // URL 반환
    @GetMapping("/{fileName}")
    public ResponseEntity<String> getImageUrl(@PathVariable String fileName) {
        String url = getS3Url(fileName);
        return ResponseEntity.ok(url);
    }

    // 삭제 (선택 사항)
    @DeleteMapping("/{fileName}")
    public ResponseEntity<String> deleteImage(@PathVariable String fileName) {
        s3Service.deleteFile(fileName);
        return ResponseEntity.ok("Deleted " + fileName);
    }

    private String getS3Url(String fileName) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                awsProperties.getBucket(), awsProperties.getRegion(), fileName);
    }

}
