package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.Entity.Story;
import com.gyeongditor.storyfield.Entity.StoryPage;
import com.gyeongditor.storyfield.Entity.User;
import com.gyeongditor.storyfield.dto.Story.SaveStoryDTO;
import com.gyeongditor.storyfield.repository.StoryRepository;
import com.gyeongditor.storyfield.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

}
