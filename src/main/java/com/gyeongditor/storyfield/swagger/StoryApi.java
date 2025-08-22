
package com.gyeongditor.storyfield.swagger;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.dto.Story.SaveStoryDTO;
import com.gyeongditor.storyfield.dto.Story.StoryPageResponseDTO;
import com.gyeongditor.storyfield.dto.Story.StoryThumbnailResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RequestMapping("/api/stories")
public interface StoryApi {

    @Operation(summary = "스토리 페이지 저장", description = "FastAPI가 생성한 스토리 페이지 데이터와 파일들을 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "스토리 저장 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                {"status":201,"code":"STORY_201_001","message":"스토리 생성 성공","data":"이야기를 저장했습니다."}
                            """))),
            @ApiResponse(responseCode = "404", description = "스토리를 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                {"status":404,"code":"STORY_404_001","message":"스토리가 존재하지 않습니다.","data":null}
                            """)))
    })
    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponseDTO<String> saveStory(
            HttpServletRequest request,
            @RequestPart("saveStoryDTO") String saveStoryDtoString,
            @RequestPart("thumbnail") MultipartFile thumbnail,
            @RequestPart("pageImages") List<MultipartFile> pageImages);

    @Operation(summary = "스토리 페이지 조회")
    @GetMapping("/{storyId}")
    ApiResponseDTO<List<StoryPageResponseDTO>> getStoryPages(HttpServletRequest request, @PathVariable UUID storyId);

    @Operation(summary = "메인 페이지 스토리 목록 조회")
    @GetMapping("/main")
    ApiResponseDTO<List<StoryThumbnailResponseDTO>> getMainPageStories(HttpServletRequest request, @RequestParam(defaultValue = "0") int page);

    @Operation(summary = "스토리 삭제", parameters = {
            @Parameter(name = "Authorization", description = "Bearer {accessToken}", required = true)
    })
    @DeleteMapping("/{storyId}")
    ApiResponseDTO<Void> deleteStory(HttpServletRequest request, @PathVariable UUID storyId);
}
