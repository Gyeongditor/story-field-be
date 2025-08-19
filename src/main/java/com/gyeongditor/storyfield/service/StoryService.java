package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.Entity.Story;
import com.gyeongditor.storyfield.Entity.StoryPage;
import com.gyeongditor.storyfield.Entity.User;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.dto.Story.*;
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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoryService {

    private final UserRepository userRepository;
    private final StoryRepository storyRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final S3Service s3Service;
    private final UserService userService;

    /**
     * FastAPI 결과 저장
     */
    @Transactional
    public ApiResponseDTO<String> saveStoryFromFastApi(
            String accessToken,
            SaveStoryDTO saveStoryDTO,
            MultipartFile thumbnail,
            List<MultipartFile> pageImages) throws IOException {

        // 1. 토큰 검증
        User user = userService.getUserFromToken(accessToken);

        // 2. 새 Story 생성
        Story.StoryBuilder storyBuilder = Story.builder()
                .storyId(UUID.randomUUID())
                .user(user)
                .storyTitle(saveStoryDTO.getStoryTitle()); // ✅ AI가 넘겨준 제목

        // 3. 썸네일 업로드 + UUID 붙이기
        if (thumbnail != null && !thumbnail.isEmpty()) {
            String thumbnailFileName = UUID.randomUUID() + "_" + saveStoryDTO.getThumbnailFileName();
            s3Service.uploadFile(thumbnail, thumbnailFileName);

            storyBuilder.thumbnailFileName(thumbnailFileName); // ✅ UUID 붙인 값 저장
        }

        Story story = storyBuilder.build();

        // 4. 페이지 저장
        List<StoryPageDTO> pages = saveStoryDTO.getPages();
        for (int i = 0; i < pages.size(); i++) {
            StoryPageDTO req = pages.get(i);
            MultipartFile file = pageImages.get(i);

            String fileName = UUID.randomUUID() + "_" + req.getImageFileName();
            s3Service.uploadFile(file, fileName);

            StoryPage page = StoryPage.builder()
                    .story(story)
                    .pageNumber(req.getPageNumber())
                    .content(req.getContent())
                    .imageFileName(fileName)
                    .build();

            story.getPages().add(page);
        }

        // 5. DB 저장
        storyRepository.save(story);

        return ApiResponseDTO.success(SuccessCode.STORY_201_001, "이야기를 저장했습니다.");
    }

    /**
     * 스토리 페이지 조회
     */
    public ApiResponseDTO<List<StoryPageResponseDTO>> getStoryPages(UUID storyId, String accessToken) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORY_404_001));

        List<StoryPageResponseDTO> pages = story.getPages().stream()
                .map(page -> {
                    String presignedUrl = s3Service
                            .generatePresignedUrl(page.getImageFileName(), accessToken) // ✅ S3Service 활용
                            .getData(); // ApiResponseDTO<String> 에서 URL 꺼내기

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


    /**
     * 메인 페이지 스토리 목록 조회
     */
    public ApiResponseDTO<List<StoryThumbnailResponseDTO>> getMainPageStories(int page, String accessToken) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Story> storyPage = storyRepository.findAll(pageable);

        List<StoryThumbnailResponseDTO> thumbnails = storyPage.getContent().stream()
                .map(story -> {
                    String thumbnailFile = story.getThumbnailFileName(); // ✅ 엔티티 필드 활용
                    String presignedThumbnail = null;

                    if (thumbnailFile != null) {
                        presignedThumbnail = s3Service
                                .generatePresignedUrl(thumbnailFile, accessToken) // ✅ S3Service 통해 presignedUrl 발급
                                .getData(); // ApiResponseDTO<String>에서 presignedUrl 꺼내기
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


    @Transactional
    public ApiResponseDTO<Void> deleteStory(String accessToken, UUID storyId) {
        String email = jwtTokenProvider.getEmail(accessToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_404_002, "토큰에 해당하는 사용자가 존재하지 않습니다."));

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORY_404_001));

        if (!story.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.STORY_403_001, "본인 스토리만 삭제할 수 있습니다.");
        }

        // ✅ S3에서 썸네일 삭제
        if (story.getThumbnailFileName() != null) {
            s3Service.deleteFile(story.getThumbnailFileName(), accessToken);
        }

        // ✅ S3에서 페이지 이미지 삭제
        story.getPages().forEach(page ->
                s3Service.deleteFile(page.getImageFileName(), accessToken)
        );

        // ✅ DB에서 스토리 삭제 (cascade 로 Page도 같이 삭제될 것임)
        storyRepository.delete(story);

        return ApiResponseDTO.success(SuccessCode.STORY_204_001, null);
    }
}
