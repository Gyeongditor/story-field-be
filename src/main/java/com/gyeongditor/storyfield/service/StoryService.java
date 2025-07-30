package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.Entity.Story;
import com.gyeongditor.storyfield.Entity.StoryPage;
import com.gyeongditor.storyfield.Entity.User;
import com.gyeongditor.storyfield.dto.Story.SaveStoryDTO;
import com.gyeongditor.storyfield.dto.Story.StoryPageResponseDTO;
import com.gyeongditor.storyfield.dto.Story.StoryThumbnailResponseDTO;
import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.repository.StoryRepository;
import com.gyeongditor.storyfield.repository.UserRepository;
import com.gyeongditor.storyfield.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoryService {

    private final UserRepository userRepository;
    private final StoryRepository storyRepository;

    /**
     * 스토리 저장
     */
    public String saveStory(UUID userId, SaveStoryDTO dto) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_404_001));

        // Story 생성
        Story story = Story.builder()
                .user(user)
                .storyTitle(dto.getStoryTitle())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // StoryPage 생성 및 연관관계 설정
        List<StoryPage> pages = dto.getPages().stream()
                .map(pageDto -> StoryPage.builder()
                        .pageNumber(pageDto.getPageNumber())
                        .content(pageDto.getContent())
                        .imageUrl(pageDto.getImageUrl())
                        .story(story)
                        .build())
                .toList();

        story.getPages().addAll(pages);

        // 저장
        storyRepository.save(story);

        return story.getStoryId().toString();
    }

    /**
     * 스토리 페이지 조회
     */
    public List<StoryPageResponseDTO> getStoryPages(UUID storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORY_404_001));

        return story.getPages().stream()
                .map(page -> StoryPageResponseDTO.builder()
                        .pageNumber(page.getPageNumber())
                        .content(page.getContent())
                        .imageUrl(page.getImageUrl())
                        .build())
                .toList();
    }

    /**
     * 메인 페이지 스토리 목록 조회
     */
    public List<StoryThumbnailResponseDTO> getMainPageStories(int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Story> storyPage = storyRepository.findAll(pageable);

        return storyPage.getContent().stream()
                .map(story -> {
                    String thumbnail = story.getPages().stream()
                            .filter(p -> p.getPageNumber() == 3)
                            .findFirst()
                            .map(StoryPage::getImageUrl)
                            .orElse(null);

                    return StoryThumbnailResponseDTO.builder()
                            .storyId(story.getStoryId())
                            .storyTitle(story.getStoryTitle())
                            .thumbnailUrl(thumbnail)
                            .build();
                })
                .toList();
    }

    /**
     * 스토리 삭제
     */
    public void deleteStory(UUID userId, UUID storyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_404_001));

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORY_404_001));

        // 권한 체크 (현재는 userId만 비교)
        if (!story.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.STORY_403_001, "본인 스토리만 삭제할 수 있습니다.");
        }

        storyRepository.delete(story);
    }
}
