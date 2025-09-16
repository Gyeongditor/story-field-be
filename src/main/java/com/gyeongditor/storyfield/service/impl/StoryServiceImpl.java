package com.gyeongditor.storyfield.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyeongditor.storyfield.Entity.Story;
import com.gyeongditor.storyfield.Entity.StoryPage;
import com.gyeongditor.storyfield.Entity.User;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.dto.Story.SaveStoryDTO;
import com.gyeongditor.storyfield.dto.Story.StoryPageDTO;
import com.gyeongditor.storyfield.dto.Story.StoryPageResponseDTO;
import com.gyeongditor.storyfield.dto.Story.StoryThumbnailResponseDTO;
import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.jwt.JwtTokenProvider;
import com.gyeongditor.storyfield.repository.StoryRepository;
import com.gyeongditor.storyfield.repository.UserRepository;
import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.response.SuccessCode;
import com.gyeongditor.storyfield.service.AuthService;
import com.gyeongditor.storyfield.service.S3Service;
import com.gyeongditor.storyfield.service.StoryService;
import com.gyeongditor.storyfield.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.zip.GZIPInputStream;

@Service
@RequiredArgsConstructor
public class StoryServiceImpl implements StoryService {

    private final UserRepository userRepository;
    private final StoryRepository storyRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final S3Service s3Service;
    private final UserService userService;
    private final AuthService authService;
    private final ObjectMapper objectMapper;
    
    // 스토리 이미지 업로드용 TaskExecutor 주입 (@Autowired로 안전하게)
    @Autowired
    @Qualifier("storyImageTaskExecutor")
    private TaskExecutor taskExecutor;

    @Override
    @Transactional
    public ApiResponseDTO<String> saveStoryFromFastApi(
            HttpServletRequest request,
            String saveStoryDtoString,
            MultipartFile thumbnailGz,
            List<MultipartFile> pageImagesGz
    ) throws IOException {

        final String accessToken = authService.extractAccessToken(request);
        final User user = userService.getUserFromToken(accessToken);

        final SaveStoryDTO saveStoryDTO;
        try {
            saveStoryDTO = objectMapper.readValue(saveStoryDtoString, SaveStoryDTO.class);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.REQ_400_001, "스토리 데이터 변환 실패");
        }

        // 썸네일 업로드 → 비동기 처리
        CompletableFuture<String> thumbnailFuture = CompletableFuture.supplyAsync(() -> {
            try {
                byte[] png = gunzipToBytes(thumbnailGz);
                return uploadPngWithUuidNaming(thumbnailGz.getOriginalFilename(), png);
            } catch (IOException e) {
                throw new CompletionException("썸네일 업로드 실패", e);
            }
        }, taskExecutor);

        // 스토리 페이지 이미지 업로드 → 비동기 처리
        List<CompletableFuture<String>> pageImageFutures = pageImagesGz.stream()
            .map(gz -> CompletableFuture.supplyAsync(() -> {
                try {
                    byte[] png = gunzipToBytes(gz);
                    return uploadPngWithUuidNaming(gz.getOriginalFilename(), png);
                } catch (IOException e) {
                    throw new CompletionException("페이지 이미지 업로드 실패: " + gz.getOriginalFilename(), e);
                }
            }, taskExecutor))
            .toList();

        // 모든 업로드 완료 후 결과 수집 (join())
        final String thumbnailKey;
        final List<String> pageImageKeys;
        
        try {
            thumbnailKey = thumbnailFuture.join();
            pageImageKeys = pageImageFutures.stream()
                .map(CompletableFuture::join)
                .toList();
        } catch (CompletionException e) {
            // 업로드 실패 시 명확한 오류 코드와 메시지 반환
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw new CustomException(ErrorCode.FILE_500_001, "이미지 업로드 중 오류 발생: " + e.getMessage());
            }
            throw new CustomException(ErrorCode.SERVER_500_001, "이미지 처리 중 예기치 않은 오류 발생: " + e.getMessage());
        }

        final List<StoryPageDTO> pages = saveStoryDTO.getPages();
        if (pages.size() != pageImageKeys.size()) {
            throw new CustomException(ErrorCode.STORY_400_001,
                    String.format("페이지 수(%d)와 이미지 파일 수(%d)가 일치하지 않습니다.", pages.size(), pageImageKeys.size()));
        }

        final Story story = Story.builder()
                .storyId(UUID.randomUUID())
                .user(user)
                .storyTitle(saveStoryDTO.getStoryTitle())
                .thumbnailFileName(thumbnailKey)
                .createdAt(LocalDateTime.now())
                .build();

        for (int i = 0; i < pages.size(); i++) {
            StoryPageDTO req = pages.get(i);
            story.getPages().add(
                    StoryPage.builder()
                            .story(story)
                            .pageNumber(req.getPageNumber())
                            .content(req.getContent())
                            .imageFileName(pageImageKeys.get(i))
                            .build()
            );
        }

        storyRepository.save(story);

        return ApiResponseDTO.success(SuccessCode.STORY_201_001, "이야기를 저장했습니다.");
    }

    private byte[] gunzipToBytes(MultipartFile gzFile) {
        if (gzFile == null || gzFile.isEmpty()) {
            throw new CustomException(ErrorCode.REQ_400_001, "빈 gzip 파일입니다.");
        }
        try (InputStream in = gzFile.getInputStream();
             GZIPInputStream gzin = new GZIPInputStream(in);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            gzin.transferTo(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new CustomException(ErrorCode.REQ_400_001,
                    "gzip 해제 실패: " + safeName(gzFile.getOriginalFilename()));
        }
    }

    private String uploadPngWithUuidNaming(String originalGzName, byte[] pngBytes) throws IOException {
        String base = stripGzExtension(safeName(originalGzName));
        String ensuredPng = ensurePngExtension(base);
        String objectKey = UUID.randomUUID() + "_" + ensuredPng;
        return s3Service.uploadBytes(pngBytes, objectKey, "image/png");
        // DB에는 objectKey를 저장합니다.
    }

    private static String safeName(String name) {
        if (name == null || name.isBlank()) return "file.png.gz";
        return Paths.get(name).getFileName().toString();
    }

    private static String stripGzExtension(String name) {
        if (name.toLowerCase(Locale.ROOT).endsWith(".gz")) {
            return name.substring(0, name.length() - 3);
        }
        return name;
    }

    private static String ensurePngExtension(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) return name;
        return name + ".png";
    }

    @Override
    public ApiResponseDTO<List<StoryPageResponseDTO>> getStoryPages(UUID storyId, HttpServletRequest request) {
        String accessToken = authService.extractAccessToken(request);

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORY_404_001));

        List<StoryPageResponseDTO> pages = story.getPages().stream()
                .map(page -> {
                    String presignedUrl = s3Service
                            .generatePresignedUrl(page.getImageFileName(), accessToken)
                            .getData();

                    return StoryPageResponseDTO.builder()
                            .pageNumber(page.getPageNumber())
                            .content(page.getContent())
                            .imageFileName(page.getImageFileName())
                            .presignedUrl(presignedUrl)
                            .build();
                })
                .toList();

        return ApiResponseDTO.success(SuccessCode.STORY_200_001, pages);
    }

    @Override
    public ApiResponseDTO<List<StoryThumbnailResponseDTO>> getMainPageStories(int page, HttpServletRequest request) {
        String accessToken = authService.extractAccessToken(request);

        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Story> storyPage = storyRepository.findAll(pageable);

        List<StoryThumbnailResponseDTO> thumbnails = storyPage.getContent().stream()
                .map(story -> {
                    String thumbnailFile = story.getThumbnailFileName();
                    String presignedThumbnail = null;

                    if (thumbnailFile != null) {
                        presignedThumbnail = s3Service
                                .generatePresignedUrl(thumbnailFile, accessToken)
                                .getData();
                    }

                    return StoryThumbnailResponseDTO.builder()
                            .storyId(story.getStoryId())
                            .storyTitle(story.getStoryTitle())
                            .thumbnailUrl(presignedThumbnail)
                            .build();
                })
                .toList();

        return ApiResponseDTO.success(SuccessCode.STORY_200_002, thumbnails);
    }

    @Override
    @Transactional
    public ApiResponseDTO<Void> deleteStory(HttpServletRequest request, UUID storyId) {
        String accessToken = authService.extractAccessToken(request);

        String email = jwtTokenProvider.getEmail(accessToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_404_002, "토큰에 해당하는 사용자가 존재하지 않습니다."));

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORY_404_001));

        if (!story.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.STORY_403_001, "본인 스토리만 삭제할 수 있습니다.");
        }

        if (story.getThumbnailFileName() != null) {
            s3Service.deleteFile(story.getThumbnailFileName(), request);
        }

        story.getPages().forEach(page -> s3Service.deleteFile(page.getImageFileName(), request));

        storyRepository.delete(story);

        return ApiResponseDTO.success(SuccessCode.STORY_204_001, null);
    }
}