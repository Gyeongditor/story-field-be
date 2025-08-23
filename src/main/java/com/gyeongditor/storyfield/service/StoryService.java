package com.gyeongditor.storyfield.service;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.dto.Story.StoryPageResponseDTO;
import com.gyeongditor.storyfield.dto.Story.StoryThumbnailResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface StoryService {

    ApiResponseDTO<String> saveStoryFromFastApi(HttpServletRequest request,
                                                String saveStoryDtoString,
                                                MultipartFile thumbnailGz,
                                                List<MultipartFile> pageImagesGz) throws IOException;

    ApiResponseDTO<List<StoryPageResponseDTO>> getStoryPages(UUID storyId, HttpServletRequest request);

    ApiResponseDTO<List<StoryThumbnailResponseDTO>> getMainPageStories(int page, HttpServletRequest request);

    ApiResponseDTO<Void> deleteStory(HttpServletRequest request, UUID storyId);
}