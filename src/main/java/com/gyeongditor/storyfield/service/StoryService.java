package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.Entity.Story;
import com.gyeongditor.storyfield.Entity.StoryPage;
import com.gyeongditor.storyfield.Entity.User;
import com.gyeongditor.storyfield.dto.Story.SaveStoryDTO;
import com.gyeongditor.storyfield.dto.Story.StoryPageResponseDTO;
import com.gyeongditor.storyfield.dto.Story.StoryThumbnailResponseDTO;
import com.gyeongditor.storyfield.repository.StoryRepository;
import com.gyeongditor.storyfield.repository.UserRepository;
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

    public String saveStory(UUID userId, SaveStoryDTO dto) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

        // 2. Story 엔티티 생성
        Story story = Story.builder()
                .user(user)
                .storyTitle(dto.getStoryTitle())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 3. StoryPage 목록 생성 및 연결
        List<StoryPage> pages = dto.getPages().stream()
                .map(pageDto -> StoryPage.builder()
                        .pageNumber(pageDto.getPageNumber())
                        .content(pageDto.getContent())
                        .imageUrl(pageDto.getImageUrl())
                        .story(story)  // 연관관계 설정
                        .build())
                .toList();

        // 4. 양방향 연관관계 설정
        story.getPages().addAll(pages);

        // 5. 저장
        storyRepository.save(story);

        return story.getStoryId().toString();
    }

    public List<StoryPageResponseDTO> getStoryPages(UUID storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 스토리가 존재하지 않습니다."));

        return story.getPages().stream()
                .map(page -> StoryPageResponseDTO.builder()
                        .pageNumber(page.getPageNumber())
                        .content(page.getContent())
                        .imageUrl(page.getImageUrl())
                        .build())
                .toList();
    }

    public List<StoryThumbnailResponseDTO> getMainPageStories(int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Story> storyPage = storyRepository.findAll(pageable);

        return storyPage.getContent().stream()
                .map(story -> {
                    String thumbnail = story.getPages().stream()
                            .filter(p -> p.getPageNumber() == 4)
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

    public void deleteStory(UUID storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 스토리가 존재하지 않습니다."));

        storyRepository.delete(story);
    }
}
