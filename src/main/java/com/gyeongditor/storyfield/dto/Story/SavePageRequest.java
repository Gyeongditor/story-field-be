package com.gyeongditor.storyfield.dto.Story;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * FastAPI -> Spring 으로 전달되는 스토리 페이지 저장 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Schema(description = "스토리 페이지 저장 요청 DTO (FastAPI -> Spring)")
public class SavePageRequest {

    @Schema(description = "페이지 번호", example = "1")
    private int pageNum;

    @Schema(description = "페이지 내용", example = "옛날옛날에 병아리들이 수영을 나갔어요")
    private String content;

    @Schema(description = "스토리 이미지 파일명 (S3 업로드용 UUID_원본파일명)", example = "550e8400-e29b-41d4-a716-446655440000_page1.png")
    private String fileName;
}
