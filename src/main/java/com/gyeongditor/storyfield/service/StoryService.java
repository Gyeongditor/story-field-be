package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.Entity.Story;
import com.gyeongditor.storyfield.Entity.StoryPage;
import com.gyeongditor.storyfield.Entity.User;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.dto.Story.SaveStoryDTO;
import com.gyeongditor.storyfield.dto.Story.StoryPageResponseDTO;
import com.gyeongditor.storyfield.dto.Story.StoryThumbnailResponseDTO;
import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.jwt.JwtTokenProvider;
import com.gyeongditor.storyfield.repository.StoryRepository;
import com.gyeongditor.storyfield.repository.UserRepository;
import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoryService {

    private final UserRepository userRepository;
    private final StoryRepository storyRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 스토리 저장
     */
    public ApiResponseDTO<String> saveStory(String accessToken, SaveStoryDTO dto) {
        String email = jwtTokenProvider.getEmail(accessToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_404_002, "토큰에 해당하는 사용자가 존재하지 않습니다."));

        Story story = Story.builder()
                .user(user)
                .storyTitle(dto.getStoryTitle())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<StoryPage> pages = dto.getPages().stream()
                .map(pageDto -> StoryPage.builder()
                        .pageNumber(pageDto.getPageNumber())
                        .content(pageDto.getContent())
                        .imageFileName(pageDto.getImageFileName())
                        .story(story)
                        .build())
                .toList();

        story.getPages().addAll(pages);
        storyRepository.save(story);

        return ApiResponseDTO.success(SuccessCode.STORY_201_001, story.getStoryId().toString());
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
                    String thumbnail = story.getPages().stream()
                            .filter(p -> p.getPageNumber() == 3)
                            .findFirst()
                            .map(StoryPage::getImageFileName)
                            .orElse(null);

                    return StoryThumbnailResponseDTO.builder()
                            .storyId(story.getStoryId())
                            .storyTitle(story.getStoryTitle())
                            .thumbnailUrl(thumbnail)
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
}
