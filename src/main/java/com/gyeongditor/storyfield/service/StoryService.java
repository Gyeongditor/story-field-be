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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
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
    private final AuthService authService;

    @Transactional
    public ApiResponseDTO<String> saveStoryFromFastApi(HttpServletRequest request, SaveStoryDTO saveStoryDTO, MultipartFile file, List<MultipartFile> files) throws IOException {

        // 1. 토큰 → User 조회
        String accessToken = authService.extractAccessToken(request);
        User user = userService.getUserFromToken(accessToken);

        // 2. 썸네일 파일 업로드 (단일 파일)
        String thumbnailFileName = s3Service.uploadThumbnailFile(file, request);

        // 3. 스토리 페이지 이미지 파일들 업로드 (리스트) - 반복문 밖에서 한 번만 실행
        List<String> pageImageFileNames = s3Service.uploadFiles(files, accessToken);

        // 4. Story 엔티티 생성
        Story story = Story.builder()
                .storyId(UUID.randomUUID())
                .user(user)
                .storyTitle(saveStoryDTO.getStoryTitle())
                .thumbnailFileName(thumbnailFileName)
                .createdAt(LocalDateTime.now())
                .build();

        // 5. Story Page 생성 및 매핑
        List<StoryPageDTO> pages = saveStoryDTO.getPages();

        // 추가: 페이지 수와 파일 수가 다를 경우를 대비한 방어 코드
        if (pages.size() != pageImageFileNames.size()) {
            // 이 부분에 예외 처리 로직을 추가하는 것이 좋습니다.
            throw new IllegalArgumentException("페이지 수와 이미지 파일 수가 일치하지 않습니다.");
        }

        for (int i = 0; i < pages.size(); i++) {
            StoryPageDTO req = pages.get(i);
            // 미리 업로드된 파일 이름 리스트에서 순서에 맞는 파일명을 가져옴
            String fileName = pageImageFileNames.get(i);

            StoryPage page = StoryPage.builder()
                    .story(story)
                    .pageNumber(req.getPageNumber())
                    .content(req.getContent())
                    .imageFileName(fileName)
                    .build();

            story.getPages().add(page);
        }

        storyRepository.save(story);

        return ApiResponseDTO.success(SuccessCode.STORY_201_001, "이야기를 저장했습니다.");
    }

    // 스토리 페이지 조회
    public ApiResponseDTO<List<StoryPageResponseDTO>> getStoryPages(UUID storyId, HttpServletRequest request) {
        String accessToken = authService.extractAccessToken(request);

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORY_404_001));

        List<StoryPageResponseDTO> pages = story.getPages().stream()
                .map(page -> {
                    String presignedUrl = s3Service
                            .generatePresignedUrl(page.getImageFileName(), accessToken) // S3Service 활용
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


    // 메인 페이지 스토리 목록 조회
    public ApiResponseDTO<List<StoryThumbnailResponseDTO>> getMainPageStories(int page, HttpServletRequest request) {
        String accessToken = authService.extractAccessToken(request);

        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Story> storyPage = storyRepository.findAll(pageable);

        List<StoryThumbnailResponseDTO> thumbnails = storyPage.getContent().stream()
                .map(story -> {
                    String thumbnailFile = story.getThumbnailFileName(); // 엔티티 필드 활용
                    String presignedThumbnail = null;

                    if (thumbnailFile != null) {
                        presignedThumbnail = s3Service
                                .generatePresignedUrl(thumbnailFile, accessToken) // S3Service 통해 presignedUrl 발급
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

        // S3에서 썸네일 삭제
        if (story.getThumbnailFileName() != null) {
            s3Service.deleteFile(story.getThumbnailFileName(), request);
        }

        // S3에서 페이지 이미지 삭제
        story.getPages().forEach(page ->
                s3Service.deleteFile(page.getImageFileName(), request)
        );

        // DB에서 스토리 삭제 (cascade 로 Page도 같이 삭제될 것임)
        storyRepository.delete(story);

        return ApiResponseDTO.success(SuccessCode.STORY_204_001, null);
    }
}
