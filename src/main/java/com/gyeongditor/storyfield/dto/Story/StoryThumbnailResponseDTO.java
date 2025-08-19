package com.gyeongditor.storyfield.dto.Story;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "스토리 썸네일 응답 DTO")
public class StoryThumbnailResponseDTO {

    @Schema(description = "스토리 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID storyId;

    @Schema(description = "스토리 제목", example = "용감한 병아리의 모험")
    private String storyTitle;

    @Schema(description = "썸네일 이미지 URL (3번 페이지 기준)", example = "https://example.com/image3.png")
    private String thumbnailUrl;
}
