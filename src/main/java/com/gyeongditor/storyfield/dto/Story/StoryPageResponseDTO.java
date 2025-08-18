package com.gyeongditor.storyfield.dto.Story;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "스토리 페이지 응답 DTO")
public class StoryPageResponseDTO {

    @Schema(description = "페이지 번호", example = "1")
    private int pageNumber;

    @Schema(description = "페이지 내용", example = "옛날 옛적에 병아리가 있었습니다.")
    private String content;

    @Schema(description = "이미지 파일명", example = "xxx.png")
    private String imageFileName;

    @Schema(description = "이미지 presignedUrl", example = "S3.xxx.xxx")
    private String presignedUrl;    // 서비스 로직에서 추가해서 내려줌

}
