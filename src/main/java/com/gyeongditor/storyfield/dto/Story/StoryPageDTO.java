package com.gyeongditor.storyfield.dto.Story;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "스토리 페이지 정보 DTO")
public class StoryPageDTO {

    @Schema(description = "페이지 번호", example = "1")
    private int pageNumber;

    @Schema(description = "페이지 내용", example = "옛날 옛적에, 병아리가 숲 속을 걸었어요.")
    private String content;

    @Schema(description = "페이지 이미지 URL", example = "https://example.com/images/page1.png")
    private String imageUrl;
}
