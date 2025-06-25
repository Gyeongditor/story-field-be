package com.gyeongditor.storyfield.dto.StoryDTO;
import lombok.Data;

import java.util.List;

@Data
public class StoryCreateRequestDTO {
    private String userId;
    private String characterType; // 동물, 용, 뭐 등등
    private List<String> keywordList; // 교훈 리스트 중에 좀 선택
    private String setting; // "숲", "바다", "우주", "학교" 등 공간적 배경
    private String emotionTone; // 기분이나 성향 "funny", "touching", "mysterious", "brave" 등
    private int length;
}
