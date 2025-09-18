package com.gyeongditor.storyfield.dto.Story;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "스토리 페이지 정보 DTO")
public class StoryPageDTO {

    @Schema(description = "페이지 번호", example = "1")
    private int pageNumber;

    @Schema(description = "페이지 내용", example = "옛날 옛적에, 병아리가 숲 속을 걸었어요.")
    private String content;

    @Schema(description = "이미지 파일명", example = "xxx.png")
    private String imageFileName;

    @Schema(description = "썸네일 파일명", example = "thumb_page_1.png")
    private String thumbnailFileName;  // ✅ 새 필드
}
