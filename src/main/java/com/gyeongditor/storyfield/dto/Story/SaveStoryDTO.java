package com.gyeongditor.storyfield.dto.Story;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "스토리 저장 요청 DTO")
public class SaveStoryDTO {
    @Schema(description = "스토리 제목", example = "용감한 병아리의 모험")
    private String storyTitle;

    @Schema(description = "스토리 페이지 리스트")
    private List<StoryPageDTO> pages;
}
