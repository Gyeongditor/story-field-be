package com.gyeongditor.storyfield.handler.mapper;

import com.gyeongditor.storyfield.response.SuccessCode;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * 요청 URI와 HTTP Method에 따라 SuccessCode를 결정한다.
 * 가장 먼저 매칭되는 패턴이 우선한다(LinkedHashMap 유지).
 */
@Component
public class SuccessCodeMapper {

    private final Map<BiFunction<String, HttpMethod, Boolean>, SuccessCode> rules = new LinkedHashMap<>();

    public SuccessCodeMapper() {
        // ===== 사용자 관련 =====
        rule((p, m) -> m == HttpMethod.POST   && p.matches("^/users/signup/?$"),              SuccessCode.USER_201_001); // 회원가입
        rule((p, m) -> m == HttpMethod.GET    && p.matches("^/users/me/?$"),        SuccessCode.USER_200_001); // 회원 조회
        rule((p, m) -> (m == HttpMethod.PUT || m == HttpMethod.PATCH) && p.matches("^/users/me+/?$"), SuccessCode.USER_200_002); // 회원 수정
        rule((p, m) -> m == HttpMethod.POST   && p.matches("^/users/verify/?$"), SuccessCode.USER_200_003); // 이메일 인증 완료
        rule((p, m) -> m == HttpMethod.DELETE && p.matches("^/users/me/[^/]+/?$"),        SuccessCode.USER_204_001); // 회원 삭제

        // ===== 인증/Auth =====
        rule((p, m) -> m == HttpMethod.POST   && p.matches("^/auth/login/?$"),         SuccessCode.AUTH_200_001); // 로그인
        rule((p, m) -> m == HttpMethod.POST   && p.matches("^/auth/logout/?$"),        SuccessCode.AUTH_200_002); // 로그아웃
        rule((p, m) -> m == HttpMethod.GET    && p.matches("^/auth/me/?$"),            SuccessCode.AUTH_200_003); // 사용자 정보 로드
        rule((p, m) -> m == HttpMethod.GET    && p.matches("^/auth/status/?$"),        SuccessCode.AUTH_200_004); // 계정 상태 정상
        rule((p, m) -> m == HttpMethod.POST   && p.matches("^/auth/login/failed/?$"),  SuccessCode.AUTH_200_006); // 실패 횟수 업데이트(예시)
        rule((p, m) -> m == HttpMethod.POST   && p.matches("^/auth/login/reset/?$"),   SuccessCode.AUTH_200_005); // 실패 횟수 초기화(예시)

        // ===== 메일 ===== path 미사용
//        rule((p, m) -> m == HttpMethod.POST   && p.matches("^/mail/verification/?$"),  SuccessCode.MAIL_200_001); // 인증 메일 전송
//        rule((p, m) -> m == HttpMethod.POST   && p.matches("^/mail/verify/?$"),        SuccessCode.MAIL_200_002); // 이메일 인증 완료

        // ===== 스토리 =====
        rule((p, m) -> m == HttpMethod.POST   && p.matches("^/stories/?$"),            SuccessCode.STORY_201_001); // 스토리 생성
        rule((p, m) -> m == HttpMethod.GET    && p.matches("^/stories/[^/]+/pages/?$"),      SuccessCode.STORY_200_001); // 스토리 페이지 조회
        rule((p, m) -> m == HttpMethod.GET    && p.matches("^/stories/thumbnails/?$"),            SuccessCode.STORY_200_002); // 메인 목록 조회
        rule((p, m) -> m == HttpMethod.DELETE && p.matches("^/stories/[^/]+/?$"),      SuccessCode.STORY_204_001); // 스토리 삭제

        // ===== OAuth2 ===== path 미사용
//        rule((p, m) -> m == HttpMethod.GET    && p.matches("^/login/oauth2/code/.*$"),     SuccessCode.OAUTH2_200_001); // OAuth2 로그인 콜백 성공
//        rule((p, m) -> m == HttpMethod.POST   && p.matches("^/oauth2/users/?$"),       SuccessCode.OAUTH2_201_001); // 신규 OAuth2 사용자 생성

        // ===== 파일 =====
        rule((p, m) -> m == HttpMethod.POST   && p.matches("^/images/?$"),       SuccessCode.FILE_200_001); // 파일 업로드
        rule((p, m) -> m == HttpMethod.GET    && p.matches("^/images/presign/?$"),      SuccessCode.FILE_200_002); // Presigned URL
        rule((p, m) -> m == HttpMethod.GET    && p.matches("^/images/[^/]+/?$"),          SuccessCode.FILE_200_003);   // 파일 URL 조회
        rule((p, m) -> m == HttpMethod.DELETE && p.matches("^/images/[^/]+/?$"),        SuccessCode.FILE_204_001); // 파일 삭제
    }

    private void rule(BiFunction<String, HttpMethod, Boolean> predicate, SuccessCode code) {
        rules.put(predicate, code);
    }

    /**
     * 우선순위 규칙 → 도메인 성공코드 → 메서드 기본코드
     */
    public SuccessCode resolve(String path, HttpMethod method, HttpStatus decidedStatusByMethod) {
        // 1) 등록된 규칙 우선
        for (var e : rules.entrySet()) {
            if (e.getKey().apply(path, method)) {
                return e.getValue();
            }
        }
        // 2) 메서드/상태 기본 코드
        if (decidedStatusByMethod == HttpStatus.CREATED)  return SuccessCode.SUCCESS_201_001;
        if (decidedStatusByMethod == HttpStatus.NO_CONTENT) return SuccessCode.SUCCESS_204_001;
        return SuccessCode.SUCCESS_200_001;
    }
}
