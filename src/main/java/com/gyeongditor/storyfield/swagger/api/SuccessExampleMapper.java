package com.gyeongditor.storyfield.swagger.api;

import com.gyeongditor.storyfield.response.SuccessCode;

import java.util.HashMap;
import java.util.Map;

public class SuccessExampleMapper {

    private static final Map<SuccessCode, String> examples = new HashMap<>();

    static {
        // 사용자 조회 (USER_200_001)
        examples.put(SuccessCode.USER_200_001, """
            {
              "status": 200,
              "code": "USER_200_001",
              "message": "회원 정보 조회가 성공적으로 완료되었습니다.",
              "data": {
                "email": "seoun@example.com",
                "username": "승일"
              }
            }
        """);

        // 로그인 성공 (AUTH_200_001)
        examples.put(SuccessCode.AUTH_200_001, """
            {
              "status": 200,
              "code": "AUTH_200_001",
              "message": "로그인이 성공적으로 완료되었습니다.",
              "data": {
                 "로그인 상태": "성공"
              }
            }
        """);

        // 스토리 생성 성공 (STORY_201_001)
        examples.put(SuccessCode.STORY_201_001, """
            {
              "status": 201,
              "code": "STORY_201_001",
              "message": "스토리가 성공적으로 생성되었습니다.",
              "data": {
                "storyId": "6ec5c4b8-a31a-46bb-8029-f873490eac89",
                "storyTitle": "내 첫 번째 동화",
                "thumbnailUrl": "https://storyfield-image-bucket.s3.ap-northeast-2.amazonaws.com/xxx_thumb.png",
                "createdAt": "2025-08-22T21:15:00"
              }
            }
        """);

        // 스토리 페이지 조회 (STORY_200_001)
        examples.put(SuccessCode.STORY_200_001, """
            {
              "status": 200,
              "code": "STORY_200_001",
              "message": "스토리 페이지가 성공적으로 조회되었습니다.",
              "data": [
                {
                  "pageNumber": 1,
                  "content": "옛날 옛적에, 숲속 마을에...",
                  "imageUrl": "https://storyfield-image-bucket.s3.ap-northeast-2.amazonaws.com/page1.png"
                },
                {
                  "pageNumber": 2,
                  "content": "주인공은 신비한 모험을 시작했습니다.",
                  "imageUrl": "https://storyfield-image-bucket.s3.ap-northeast-2.amazonaws.com/page2.png"
                }
              ]
            }
        """);

        // 메인 페이지 스토리 목록 조회 (STORY_200_002)
        examples.put(SuccessCode.STORY_200_002, """
            {
              "status": 200,
              "code": "STORY_200_002",
              "message": "메인 페이지 스토리 목록이 성공적으로 조회되었습니다.",
              "data": [
                {
                  "storyId": "6ec5c4b8-a31a-46bb-8029-f873490eac89",
                  "storyTitle": "여승철과 매직소주",
                  "thumbnailUrl": "https://presignedUrl1/abc_thumb.png"
                },
                {
                  "storyId": "d6ab7847-4d34-47b4-85ea-d2f773eab4f0",
                  "storyTitle": "내 첫 번째 동화",
                  "thumbnailUrl": "https://presignedUrl12/def_thumb.png"
                }
              ]
            }
        """);

        // 오디오 업로드 성공 (AUDIO_200_001)
        examples.put(SuccessCode.AUDIO_200_001, """
            {
              "status": 200,
              "code": "AUDIO_200_001",
              "message": "오디오 파일 업로드 성공",
              "data": "https://storyfield-audio-bucket.s3.ap-northeast-2.amazonaws.com/uuid_name.m4a"
            }
        """);
    }

    public static String getExample(SuccessCode code) {
        return examples.getOrDefault(code, "null");
    }
}
