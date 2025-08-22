package com.gyeongditor.storyfield.Controller;


import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.service.AudioService;
import com.gyeongditor.storyfield.swagger.AudioApi;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class AudioController implements AudioApi {

    private final AudioService audioService;

    @Override
    public ApiResponseDTO<String> uploadAudio(MultipartFile file, HttpServletRequest request) {
        return audioService.uploadAudio(file, request);
    }

    @Override
    public ApiResponseDTO<String> getAudioUrl(String fileName,  HttpServletRequest request ) {

        return audioService.getAudioUrl(fileName, request);
    }

    @Override
    public ApiResponseDTO<String> getDownloadPresignedUrl(String keyOrFileName, HttpServletRequest request) {
        return audioService.generateDownloadPresignedUrl(keyOrFileName, request);
    }

    @Override
    public ApiResponseDTO<Void> deleteAudio(String fileName, HttpServletRequest request) {
        return audioService.deleteAudio(fileName, request);
    }
}
