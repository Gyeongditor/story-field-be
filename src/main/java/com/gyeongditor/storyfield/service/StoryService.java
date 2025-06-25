package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.Entity.Story;
import com.gyeongditor.storyfield.Entity.StoryPage;
import com.gyeongditor.storyfield.Entity.User;
import com.gyeongditor.storyfield.dto.StoryDTO.StoryCreateDTO;
import com.gyeongditor.storyfield.dto.StoryDTO.StoryPageResponseDTO;
import com.gyeongditor.storyfield.dto.StoryDTO.StorySummaryDTO;
import com.gyeongditor.storyfield.repository.StoryPageRepository;
import com.gyeongditor.storyfield.repository.StoryRepository;
import com.gyeongditor.storyfield.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class StoryService {

    private final StoryRepository storyRepository;
    private final StoryPageRepository storyPageRepository;
    private final UserRepository userRepository;


    public String createStory(StoryCreateDTO request, User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        // 사용자가 작성한 모든 값으로 DTO 생성
        StoryCreateDTO storyCreateDTO = StoryCreateDTO.builder()
                .userId(userId)
                .storyTitle(request.getStoryTitle())
                .storyContent(request.getStoryContent())
                .build();

        // 저장
        Story story = storyCreateDTO.toEntity(user);
        storyRepository.save(story);

        return story.getStoryId();  // 또는 title 반환도 가능
    }


    public List<StoryPageResponseDTO> getPagesByStoryId(String storyId) {
        List<StoryPage> pages = storyPageRepository.findByStoryIdOrderByStoryPageNumAsc(storyId);
        return pages.stream()
                .map(StoryPageResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }


    public List<StorySummaryDTO> readAllStory() {
        return storyRepository.findAll().stream()
                .map(story -> {
                    Optional<StoryPage> page1 = storyPageRepository
                            .findFirstByStoryIdAndStoryPageNum(story.getStoryId(), 1);
                    return new StorySummaryDTO(
                            story.getStoryId(),
                            story.getStoryTitle(),
                            page1.isPresent() ? page1.get().getStoryImageUrl() : null
                    );
                })
                .collect(Collectors.toList());
    }


    public String deleteStory(String storyId) {
        if (!storyRepository.existsById(storyId)) {
            throw new IllegalArgumentException("삭제할 스토리가 존재하지 않습니다.");
        }
        storyRepository.deleteById(storyId);
        return storyId;
    }

}
