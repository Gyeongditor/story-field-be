package com.gyeongditor.storyfield.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyeongditor.storyfield.dto.Story.SaveStoryDTO;
import com.gyeongditor.storyfield.dto.Story.StoryPageResponseDTO;
import com.gyeongditor.storyfield.dto.Story.StoryThumbnailResponseDTO;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.service.StoryService;
import io.jsonwebtoken.io.IOException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody; // 👈 import 주의
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "Story", description = "동화")
@RestController
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;
    private final ObjectMapper objectMapper;


    @SneakyThrows
    @Operation(summary = "스토리 페이지 저장", description = "FastAPI가 생성한 스토리 페이지 데이터와 파일들을 저장합니다.",
            // ✅ requestBody를 사용하여 multipart/form-data 요청을 상세히 정의합니다.
            requestBody = @RequestBody(
                    description = "스토리 정보(JSON)와 썸네일, 페이지 이미지 파일들",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,

                            // ✅ 각 파트의 Content-Type을 명시합니다. (Swagger UI에서 더 명확하게 보임)
                            encoding = {
                                    @Encoding(name = "saveStoryDTO", contentType = "application/json"),
                                    @Encoding(name = "thumbnail", contentType = "thumb/png"),
                                    @Encoding(name = "pageImages", contentType = "image1/png"),
                                    @Encoding(name = "pageImages", contentType = "image2/png")
                            }
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "스토리 페이지 저장 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
            {
              "status": 201,
              "code": "STORY_201_001",
              "message": "스토리 생성 성공",
              "data": "이야기를 저장했습니다."
            }
            """))),
            @ApiResponse(
                    responseCode = "404",
                    description = "스토리를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
            { "status": 404, "code": "STORY_404_001", "message": "스토리가 존재하지 않습니다.", "data": null }
            """)))
    })
    // ✅ consumes 속성으로 multipart/form-data 타입을 명시합니다.
    @PostMapping(value = "/stories/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponseDTO<String> saveStory(
            HttpServletRequest request,
            @RequestPart("saveStoryDTO") String saveStoryDtoString, // 👈 JSON 데이터를 문자열로 받음
            @RequestPart("thumbnail") MultipartFile thumbnail,         // 👈 썸네일 파일
            @RequestPart("pageImages") List<MultipartFile> pageImages // 👈 페이지 이미지 파일 리스트
    ) throws IOException { // ObjectMapper가 IOException을 던질 수 있으므로 추가

        // 1. 토큰 추출
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.AUTH_401_003, "토큰이 존재하지 않습니다.");
        }

        String accessToken = authorizationHeader.substring(7).trim();
        // 2. JSON 문자열을 SaveStoryDTO 객체로 변환
        SaveStoryDTO saveStoryDTO = objectMapper.readValue(saveStoryDtoString, SaveStoryDTO.class);

        // 3. 서비스 호출 (변경된 메소드 시그니처에 맞게 호출)
        return storyService.saveStoryFromFastApi(accessToken, saveStoryDTO, thumbnail, pageImages);
    }


    @Operation(summary = "스토리 페이지 조회", description = "스토리 ID에 해당하는 전체 페이지 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
      {"status":200,"code":"STORY_200_001","message":"스토리 페이지가 성공적으로 조회되었습니다.",
       "data":[
         {"pageNumber":1,"content":"옛날 옛적에 병아리가 있었습니다.","imageFileName":"p1.png","presignedUrl":"https://..."},
         {"pageNumber":2,"content":"병아리는 숲 속 친구들을 만났습니다.","imageFileName":"p2.png","presignedUrl":"https://..."}
       ]}"""))),
            @ApiResponse(responseCode = "404", description = "스토리 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
      {"status":404,"code":"STORY_404_001","message":"스토리가 존재하지 않습니다.","data":null}""")))
    })
    @GetMapping("/api/stories/{storyId}")
    public ApiResponseDTO<List<StoryPageResponseDTO>> getStoryPages(HttpServletRequest request, @PathVariable UUID storyId) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.AUTH_401_003, "토큰이 존재하지 않습니다.");
        }

        String accessToken = authorizationHeader.substring(7).trim();
        return storyService.getStoryPages(storyId, accessToken);
    }

    @Operation(summary = "메인 페이지 스토리 목록 조회", description = "최신 스토리 썸네일 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
      {"status":200,"code":"STORY_200_002","message":"메인 페이지 스토리 목록이 성공적으로 조회되었습니다.",
       "data":[
         {"storyId":"123e4567-e89b-12d3-a456-426614174000","storyTitle":"용감한 병아리의 모험","thumbnailUrl":"https://.../thumb1.png"},
         {"storyId":"223e4567-e89b-12d3-a456-426614174000","storyTitle":"숲 속의 비밀","thumbnailUrl":"https://.../thumb2.png"}
       ]}""")))
    })
    @GetMapping("/api/stories/main")
    public ApiResponseDTO<List<StoryThumbnailResponseDTO>> getMainPageStories(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.AUTH_401_003, "토큰이 존재하지 않습니다.");
        }

        String accessToken = authorizationHeader.substring(7).trim();

        return storyService.getMainPageStories(page, accessToken);
    }

    @Operation(summary = "스토리 삭제", description = "accessToken 기반으로 본인 스토리를 삭제합니다.",
            parameters = {
                    @Parameter(name = "Authorization", description = "Bearer {accessToken}", required = true)
            })
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
      {"status":204,"code":"STORY_204_001","message":"스토리가 성공적으로 삭제되었습니다.","data":null}"""))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
      {"status":403,"code":"STORY_403_001","message":"해당 스토리에 대한 삭제 권한이 없습니다.","data":null}"""))),
            @ApiResponse(responseCode = "404", description = "스토리 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
      {"status":404,"code":"STORY_404_001","message":"스토리가 존재하지 않습니다.","data":null}""")))
    })
    @DeleteMapping("/api/stories/{storyId}")
    public ApiResponseDTO<Void> deleteStory(
            HttpServletRequest request,
            @PathVariable UUID storyId) {

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.AUTH_401_003, "토큰이 존재하지 않습니다.");
        }

        String accessToken = authorizationHeader.substring(7).trim();

        return storyService.deleteStory(accessToken, storyId);
    }
}
