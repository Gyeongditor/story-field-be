package com.gyeongditor.storyfield.service.impl;

import com.gyeongditor.storyfield.Entity.Story;
import com.gyeongditor.storyfield.Entity.StoryPage;
import com.gyeongditor.storyfield.Entity.User;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.dto.Story.SaveStoryDTO;
import com.gyeongditor.storyfield.dto.Story.StoryPageDTO;
import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.repository.StoryRepository;
import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.response.SuccessCode;
import com.gyeongditor.storyfield.service.StoryPersistenceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoryPersistenceServiceImpl implements StoryPersistenceService {

    private final StoryRepository storyRepository;

    @Override
    @Transactional
    public ApiResponseDTO<String> saveStory(
            final User user,
            final SaveStoryDTO dto,
            final String thumbnailKey,
            final List<String> pageImageKeys
    ) {
        final List<StoryPageDTO> pages = dto.getPages();
        if (pages.size() != pageImageKeys.size()) {
            throw new CustomException(
                    ErrorCode.STORY_400_004,
                    String.format(
                            "페이지 수(%d)와 이미지 파일 수(%d)가 일치하지 않습니다",
                            pages.size(),
                            pageImageKeys.size()
                    )
            );
        }

        final Story story = Story.builder()
                .storyId(UUID.randomUUID())
                .user(user)
                .storyTitle(dto.getStoryTitle())
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
}