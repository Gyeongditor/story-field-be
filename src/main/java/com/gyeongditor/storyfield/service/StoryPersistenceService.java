package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.Entity.User;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.dto.Story.SaveStoryDTO;

import java.util.List;

public interface StoryPersistenceService {

    ApiResponseDTO<String> saveStory(User user, SaveStoryDTO dto, String thumbnailKey, List<String> pageImageKeys);
}