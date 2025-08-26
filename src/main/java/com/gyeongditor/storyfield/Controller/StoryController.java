package com.gyeongditor.storyfield.Controller;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.dto.Story.StoryPageResponseDTO;
import com.gyeongditor.storyfield.dto.Story.StoryThumbnailResponseDTO;
import com.gyeongditor.storyfield.response.SuccessCode;
import com.gyeongditor.storyfield.service.StoryService;
import com.gyeongditor.storyfield.swagger.api.StoryApi;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class StoryController implements StoryApi {

    private final StoryService storyService;

    @SneakyThrows
    @Override
    public ApiResponseDTO<String> saveStory(HttpServletRequest request,
                                            String saveStoryDtoString,
                                            MultipartFile thumbnail,
                                            List<MultipartFile> pageImages) {
        return storyService.saveStoryFromFastApi(request,saveStoryDtoString, thumbnail, pageImages);
    }
    @Override
    public ApiResponseDTO<List<StoryPageResponseDTO>> getStoryPages(HttpServletRequest request, UUID storyId) {
        return storyService.getStoryPages(storyId, request);
    }

    @Override
    public ApiResponseDTO<List<StoryThumbnailResponseDTO>> getMainPageStories(HttpServletRequest request, int page) {
        return storyService.getMainPageStories(page, request);
    }

    @Override
    public ApiResponseDTO<Void> deleteStory(HttpServletRequest request, UUID storyId) {
        return storyService.deleteStory(request, storyId);
    }
}
