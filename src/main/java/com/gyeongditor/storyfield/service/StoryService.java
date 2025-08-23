package com.gyeongditor.storyfield.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

@Service
@RequiredArgsConstructor
public class StoryService {

    private final UserRepository userRepository;
    private final StoryRepository storyRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final S3Service s3Service;
    private final UserService userService;
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Transactional
    public ApiResponseDTO<String> saveStoryFromFastApi(
            HttpServletRequest request,
            String saveStoryDtoString,
            MultipartFile thumbnailGz,
            List<MultipartFile> pageImagesGz
    ) throws IOException {

        // 1) 토큰 → 유저
        final String accessToken = authService.extractAccessToken(request);
        final User user = userService.getUserFromToken(accessToken);

        // 2) DTO 파싱
        final SaveStoryDTO saveStoryDTO;
        try {
            saveStoryDTO = objectMapper.readValue(saveStoryDtoString, SaveStoryDTO.class);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.REQ_400_001, "스토리 데이터 변환 실패");
        }

        // 3) thumbnail.gz → PNG 바이트 → S3 업로드
        final byte[] thumbnailPngBytes = gunzipToBytes(thumbnailGz);
        final String thumbnailKey = uploadPngWithUuidNaming(thumbnailGz.getOriginalFilename(), thumbnailPngBytes);

        // 4) pages.gz 리스트 → PNG 바이트들 → S3 업로드
        final List<String> pageImageKeys = new ArrayList<>();
        for (MultipartFile gz : pageImagesGz) {
            byte[] png = gunzipToBytes(gz);
            pageImageKeys.add(uploadPngWithUuidNaming(gz.getOriginalFilename(), png));
        }

        // 5) 페이지 수 검증 (DTO vs 파일 수)
        final List<StoryPageDTO> pages = saveStoryDTO.getPages();
        if (pages.size() != pageImageKeys.size()) {
            throw new CustomException(ErrorCode.STORY_400_001,
                    String.format("페이지 수(%d)와 이미지 파일 수(%d)가 일치하지 않습니다.", pages.size(), pageImageKeys.size()));
        }

        // 6) Story 엔티티 생성
        final Story story = Story.builder()
                .storyId(UUID.randomUUID())
                .user(user)
                .storyTitle(saveStoryDTO.getStoryTitle())
                .thumbnailFileName(thumbnailKey)
                .createdAt(LocalDateTime.now())
                .build();

        // 7) StoryPage 매핑
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

        // 8) 저장
        storyRepository.save(story);

        // 9) 응답
        return ApiResponseDTO.success(SuccessCode.STORY_201_001, "이야기를 저장했습니다.");
    }

    /** gzip 단일 파일 해제 → 바이트 */
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

    /** 파일명 규칙 적용 후 PNG 업로드 */
    private String uploadPngWithUuidNaming(String originalGzName, byte[] pngBytes) throws IOException {
        String base = stripGzExtension(safeName(originalGzName)); // foo.png.gz → foo.png
        String ensuredPng = ensurePngExtension(base);             // foo → foo.png (확장자 보정)
        String objectKey = UUID.randomUUID() + "_" + ensuredPng;

        // Content-Type 고정: PNG
        return s3Service.uploadBytes(pngBytes, objectKey, "image/png");
    }

    private static String safeName(String name) {
        if (name == null || name.isBlank()) return "file.png.gz";
        // 경로 조작 방지: 마지막 요소만 취급
        return Paths.get(name).getFileName().toString();
    }

    private static String stripGzExtension(String name) {
        if (name.toLowerCase(Locale.ROOT).endsWith(".gz")) {
            return name.substring(0, name.length() - 3); // drop ".gz"
        }
        return name;
    }

    private static String ensurePngExtension(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) return name;
        // 만약 원본이 .webp.gz였지만 "PNG로 변환" 정책이라면 서버에서 실제 포맷 변환이 필요합니다.
        // 현재 구현은 "FastAPI가 PNG를 gzip" 한다는 전제를 두므로 확장자만 보정합니다.
        return name + ".png";
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
