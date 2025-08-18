package com.gyeongditor.storyfield.service;

import com.amazonaws.services.s3.AmazonS3;
import com.gyeongditor.storyfield.Entity.Story;
import com.gyeongditor.storyfield.Entity.StoryPage;
import com.gyeongditor.storyfield.Entity.User;
import com.gyeongditor.storyfield.config.AwsProperties;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.dto.Story.SavePageRequest;
import com.gyeongditor.storyfield.dto.Story.SaveStoryDTO;
import com.gyeongditor.storyfield.dto.Story.StoryPageResponseDTO;
import com.gyeongditor.storyfield.dto.Story.StoryThumbnailResponseDTO;
import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.jwt.JwtTokenProvider;
import com.gyeongditor.storyfield.repository.StoryRepository;
import com.gyeongditor.storyfield.repository.UserRepository;
import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.response.SuccessCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoryService {

    private final UserRepository userRepository;
    private final StoryRepository storyRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AmazonS3 amazonS3;
    private final AwsProperties awsProperties;
    private final S3Service s3Service;
    private final UserService userService;

    /**
     * PresignedUrl 생성
     */
    private String generatePresignedUrl(String fileName) {
        Date expiration = new Date(System.currentTimeMillis() + 600 * 1000); // 10분 유효
        URL url = amazonS3.generatePresignedUrl(awsProperties.getBucket(), fileName, expiration);
        return url.toString();
    }



    /**
     * 스토리 페이지 조회
     */
    public ApiResponseDTO<List<StoryPageResponseDTO>> getStoryPages(UUID storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORY_404_001));

        List<StoryPageResponseDTO> pages = story.getPages().stream()
                .map(page -> StoryPageResponseDTO.builder()
                        .pageNumber(page.getPageNumber())
                        .content(page.getContent())
                        .imageFileName(page.getImageFileName())
                        .presignedUrl(generatePresignedUrl(page.getImageFileName())) // ✅ presignedUrl 동적 생성
                        .build())
                .toList();

        return ApiResponseDTO.success(SuccessCode.STORY_200_001, pages);
    }

    /**
     * 메인 페이지 스토리 목록 조회
     */
    public ApiResponseDTO<List<StoryThumbnailResponseDTO>> getMainPageStories(int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Story> storyPage = storyRepository.findAll(pageable);

        List<StoryThumbnailResponseDTO> thumbnails = storyPage.getContent().stream()
                .map(story -> {
                    String thumbnailFile = story.getPages().stream()
                            .filter(p -> p.getPageNumber() == 3)
                            .findFirst()
                            .map(StoryPage::getImageFileName)
                            .orElse(null);

                    String presignedThumbnail = thumbnailFile != null ? generatePresignedUrl(thumbnailFile) : null;

                    return StoryThumbnailResponseDTO.builder()
                            .storyId(story.getStoryId())
                            .storyTitle(story.getStoryTitle())
                            .thumbnailUrl(presignedThumbnail)
                            .build();
                })
                .toList();

        return ApiResponseDTO.success(SuccessCode.STORY_200_002, thumbnails);
    }

    /**
     * 스토리 삭제
     */
    public ApiResponseDTO<Void> deleteStory(String accessToken, UUID storyId) {
        String email = jwtTokenProvider.getEmail(accessToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_404_002, "토큰에 해당하는 사용자가 존재하지 않습니다."));

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORY_404_001));

        if (!story.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.STORY_403_001, "본인 스토리만 삭제할 수 있습니다.");
        }

        storyRepository.delete(story);
        return ApiResponseDTO.success(SuccessCode.STORY_204_001, null);
    }

    @Transactional
    public ApiResponseDTO<String> saveStoryPagesFromFastApi(String accessToken, UUID storyId,
                                                          List<SavePageRequest> pages, List<MultipartFile> files) throws IOException {
        // 토큰 검증
        User user = userService.getUserFromToken(accessToken);
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORY_404_001));

        if (!story.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.STORY_403_001, "본인 스토리만 수정할 수 있습니다.");
        }

        for (int i = 0; i < pages.size(); i++) {
            SavePageRequest req = pages.get(i);
            MultipartFile file = files.get(i);

            // 1. 파일 업로드 → S3Service
            String fileName = UUID.randomUUID() + "_" + req.getFileName();
            s3Service.uploadFile(file, accessToken);

            // 2. StoryPage 저장
            StoryPage page = StoryPage.builder()
                    .story(story)
                    .pageNumber(req.getPageNum())
                    .content(req.getContent())
                    .imageFileName(fileName)
                    .build();

            story.getPages().add(page);
        }

        storyRepository.save(story);
        return ApiResponseDTO.success(SuccessCode.STORY_201_001, "이야기를 저장했습니다.");
    }
}
