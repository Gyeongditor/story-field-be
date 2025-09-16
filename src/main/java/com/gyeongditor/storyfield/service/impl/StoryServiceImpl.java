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
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
public class StoryServiceImpl implements StoryService {

    private final UserRepository userRepository;
    private final StoryRepository storyRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final S3Service s3Service;
    private final UserService userService;
    private final AuthService authService;
    private final ObjectMapper objectMapper;
    private final TaskExecutor taskExecutor;
    
    // 생성자 주입으로 @Qualifier 적용 및 불변성 보장
    public StoryServiceImpl(
            UserRepository userRepository,
            StoryRepository storyRepository,
            JwtTokenProvider jwtTokenProvider,
            S3Service s3Service,
            UserService userService,
            AuthService authService,
            ObjectMapper objectMapper,
            @Qualifier("storyImageTaskExecutor") TaskExecutor taskExecutor
    ) {
        this.userRepository = userRepository;
        this.storyRepository = storyRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.s3Service = s3Service;
        this.userService = userService;
        this.authService = authService;
        this.objectMapper = objectMapper;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public ApiResponseDTO<String> saveStoryFromFastApi(
            HttpServletRequest request,
            String saveStoryDtoString,
            MultipartFile thumbnailGz,
            List<MultipartFile> pageImagesGz
    ) throws IOException {

        final long totalStartTime = System.currentTimeMillis();
        final String accessToken = authService.extractAccessToken(request);
        final User user = userService.getUserFromToken(accessToken);

        final SaveStoryDTO saveStoryDTO;
        try {
            saveStoryDTO = objectMapper.readValue(saveStoryDtoString, SaveStoryDTO.class);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.REQ_400_001, "스토리 데이터 변환 실패");
        }

        // 스토리 생성 시작 로그
        log.info("스토리생성시작 userId={} title={} thumbnailSize={}KB pageCount={}", 
            user.getUserId(), saveStoryDTO.getStoryTitle(), 
            thumbnailGz.getSize() / 1024, pageImagesGz.size());

        // 1단계: 이미지 업로드 (트랜잭션 외부에서 비동기 처리)
        final String thumbnailKey;
        final List<String> pageImageKeys;
        final long uploadStartTime = System.currentTimeMillis();
        
        try {
            // 비동기 이미지 업로드 실행
            CompletableFuture<String> thumbnailFuture = uploadImageAsync(thumbnailGz, "썸네일");
            List<CompletableFuture<String>> pageImageFutures = pageImagesGz.stream()
                    .map(gz -> uploadImageAsync(gz, "페이지 이미지"))
                    .toList();

            // 모든 업로드 완료 대기
            thumbnailKey = thumbnailFuture.join();
            pageImageKeys = pageImageFutures.stream()
                    .map(CompletableFuture::join)
                    .toList();
                    
            final long uploadDuration = System.currentTimeMillis() - uploadStartTime;
            log.info("전체이미지업로드완료 userId={} uploadCount={} duration={}ms", 
                user.getUserId(), 1 + pageImagesGz.size(), uploadDuration);
                
        } catch (CompletionException e) {
            final long uploadDuration = System.currentTimeMillis() - uploadStartTime;
            log.error("전체이미지업로드실패 userId={} duration={}ms error={}", 
                user.getUserId(), uploadDuration, e.getMessage());
                
            // 업로드 실패 시 구체적인 오류 코드와 메시지 반환
            Throwable cause = e.getCause();
            String errorMessage = e.getMessage();
            
            if (cause instanceof IOException) {
                // GZIP 해제 실패
                if (errorMessage.contains("gzip 해제 실패")) {
                    throw new CustomException(ErrorCode.STORY_400_002, "압축 파일 형식이 올바르지 않습니다");
                }
                // 파일 크기 문제
                else if (errorMessage.contains("크기") || errorMessage.contains("size")) {
                    throw new CustomException(ErrorCode.STORY_413_001, "이미지 파일 크기가 너무 큽니다");
                }
                // 썸네일 업로드 실패
                else if (errorMessage.contains("썸네일")) {
                    throw new CustomException(ErrorCode.STORY_500_001, "썸네일 업로드에 실패했습니다");
                }
                // 스토리 페이지 이미지 업로드 실패
                else if (errorMessage.contains("페이지 이미지")) {
                    throw new CustomException(ErrorCode.STORY_500_002, "스토리 이미지 업로드에 실패했습니다");
                }
                // 일반적인 파일 업로드 실패
                else {
                    throw new CustomException(ErrorCode.FILE_500_001, "파일 업로드에 실패했습니다");
                }
            } else if (cause instanceof CustomException) {
                // 이미 정의된 커스텀 예외는 그대로 전파
                throw (CustomException) cause;
            } else if (cause instanceof SecurityException) {
                // 파일 접근 권한 문제
                throw new CustomException(ErrorCode.STORY_500_004, "파일 접근 권한이 없습니다");
            } else if (cause instanceof OutOfMemoryError) {
                // 메모리 부족
                throw new CustomException(ErrorCode.SERVER_500_001, "서버 리소스가 부족합니다");
            } else {
                // 예상치 못한 오류 (로그에만 상세 정보 기록)
                throw new CustomException(ErrorCode.SERVER_500_001, "파일 처리 중 오류가 발생했습니다");
            }
        }

        // 2단계: DB 저장 (트랜잭션 내에서 처리)
        final ApiResponseDTO<String> result = saveStoryToDatabase(user, saveStoryDTO, thumbnailKey, pageImageKeys);
        
        final long totalDuration = System.currentTimeMillis() - totalStartTime;
        log.info("스토리생성완료 userId={} totalDuration={}ms", 
            user.getUserId(), totalDuration);
            
        return result;
    }

    /**
     * 개별 이미지 비동기 업로드 (모니터링 포함)
     */
    private CompletableFuture<String> uploadImageAsync(final MultipartFile imageGz, final String imageType) {
        return CompletableFuture.supplyAsync(() -> {
            final long startTime = System.currentTimeMillis();
            final String fileName = safeName(imageGz.getOriginalFilename());
            final long fileSizeKB = imageGz.getSize() / 1024;
            
            try {
                final byte[] png = gunzipToBytes(imageGz);
                final String result = uploadPngWithUuidNaming(imageGz.getOriginalFilename(), png);
                
                final long duration = System.currentTimeMillis() - startTime;
                
                // 성공 모니터링 로그
                log.info("이미지업로드성공 type={} file={} size={}KB duration={}ms result={}", 
                    imageType, fileName, fileSizeKB, duration, result);
                
                return result;
                
            } catch (IOException e) {
                final long duration = System.currentTimeMillis() - startTime;
                
                // 실패 모니터링 로그
                log.error("이미지업로드실패 type={} file={} size={}KB duration={}ms error={}", 
                    imageType, fileName, fileSizeKB, duration, e.getMessage());
                
                throw new CompletionException(imageType + " 업로드 실패", e);
            } catch (Exception e) {
                final long duration = System.currentTimeMillis() - startTime;
                
                // 예상치 못한 오류 모니터링
                log.error("이미지업로드예외 type={} file={} size={}KB duration={}ms error={} class={}", 
                    imageType, fileName, fileSizeKB, duration, e.getMessage(), e.getClass().getSimpleName());
                
                throw new CompletionException(imageType + " 업로드 실패", e);
            }
        }, taskExecutor);
    }

    /**
     * 스토리 DB 저장 (트랜잭션 처리)
     */
    @Transactional
    private ApiResponseDTO<String> saveStoryToDatabase(
            User user, 
            SaveStoryDTO saveStoryDTO, 
            String thumbnailKey, 
            List<String> pageImageKeys
    ) {
        final List<StoryPageDTO> pages = saveStoryDTO.getPages();
        if (pages.size() != pageImageKeys.size()) {
            throw new CustomException(ErrorCode.STORY_400_004,
                    String.format("페이지 수(%d)와 이미지 파일 수(%d)가 일치하지 않습니다", pages.size(), pageImageKeys.size()));
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
            throw new CustomException(ErrorCode.FILE_400_001, "빈 파일입니다");
        }
        
        // 파일 크기 체크 (10MB 제한)
        if (gzFile.getSize() > 10 * 1024 * 1024) {
            throw new CustomException(ErrorCode.STORY_413_001, "파일 크기가 너무 큽니다");
        }
        
        try (InputStream in = gzFile.getInputStream();
             GZIPInputStream gzin = new GZIPInputStream(in);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            gzin.transferTo(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            String fileName = safeName(gzFile.getOriginalFilename());
            
            // GZIP 형식 오류
            if (e.getMessage().contains("Not in GZIP format") || 
                e.getMessage().contains("invalid header") ||
                e.getMessage().contains("incorrect header check")) {
                throw new CustomException(ErrorCode.STORY_400_002, 
                    "GZIP 압축 형식이 아닙니다: " + fileName);
            }
            
            // 파일 접근 권한 문제
            if (e.getMessage().contains("Access denied") || 
                e.getMessage().contains("Permission denied")) {
                throw new CustomException(ErrorCode.STORY_500_004, 
                    "파일 접근 권한이 없습니다");
            }
            
            // 일반적인 GZIP 해제 실패
            throw new CustomException(ErrorCode.STORY_400_002,
                    "압축 파일 해제에 실패했습니다: " + fileName);
        }
    }

    private String uploadPngWithUuidNaming(final String originalGzName, final byte[] pngBytes) throws IOException {
        final String base = stripGzExtension(safeName(originalGzName));
        final String ensuredPng = ensurePngExtension(base);
        final String objectKey = UUID.randomUUID() + "_" + ensuredPng;
        return s3Service.uploadBytes(pngBytes, objectKey, "image/png");
    }

    private static String safeName(final String name) {
        if (name == null || name.isBlank()) {
            return "file.png.gz";
        }
        return Paths.get(name).getFileName().toString();
    }

    private static String stripGzExtension(final String name) {
        if (name.toLowerCase(Locale.ROOT).endsWith(".gz")) {
            return name.substring(0, name.length() - 3);
        }
        return name;
    }

    private static String ensurePngExtension(final String name) {
        final String lower = name.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) {
            return name;
        }
        return name + ".png";
    }

    @Override
    public ApiResponseDTO<List<StoryPageResponseDTO>> getStoryPages(final UUID storyId, final HttpServletRequest request) {
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
    public ApiResponseDTO<List<StoryThumbnailResponseDTO>> getMainPageStories(final int page, final HttpServletRequest request) {
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
    public ApiResponseDTO<Void> deleteStory(final HttpServletRequest request, final UUID storyId) {
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