package com.gyeongditor.storyfield.handler;

import com.amazonaws.AmazonClientException;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.response.ErrorCode;
import com.gyeongditor.storyfield.response.SuccessCode;
import com.gyeongditor.storyfield.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.mail.MailException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    private final AuthService authService;

    // ✅ 공통 성공 응답 처리
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // ResponseEntity는 제외
        if (returnType.getParameterType().equals(ResponseEntity.class)) {
            return false;
        }

        // Swagger 요청은 ResponseBodyAdvice 적용 제외
        String path = returnType.getContainingClass().getName();
        jakarta.servlet.http.HttpServletRequest req =
                ((org.springframework.web.context.request.ServletRequestAttributes)
                        org.springframework.web.context.request.RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String uri = req.getRequestURI();
        if (uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger-ui")) {
            return false;
        }

        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  org.springframework.http.server.ServerHttpRequest request,
                                  org.springframework.http.server.ServerHttpResponse response) {

        if (body instanceof ApiResponseDTO) return body;

        String method = request.getMethod().name();
        HttpStatus status;
        SuccessCode successCode;

        switch (method) {
            case "POST" -> {
                status = HttpStatus.CREATED;
                successCode = SuccessCode.SUCCESS_201_001;
            }
            case "DELETE" -> {
                status = HttpStatus.NO_CONTENT;
                successCode = SuccessCode.SUCCESS_204_001;
            }
            default -> {
                status = HttpStatus.OK;
                successCode = SuccessCode.SUCCESS_200_001;
            }
        }

        response.setStatusCode(status);
        if (status == HttpStatus.NO_CONTENT) return null;

        return ApiResponseDTO.success(successCode, body);
    }

    // ✅ CustomException 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleCustomException(CustomException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        log.warn("[CustomException] code={}, message={}", errorCode.getCode(), ex.getCustomMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponseDTO.error(errorCode, ex.getCustomMessage()));
    }

    // ✅ Spring Security 인증 오류
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        String email = extractEmailFromRequest(request);
        if (email != null) {
            authService.handleLoginFailure(email);
        }

        ErrorCode errorCode = ErrorCode.AUTH_401_004;
        log.warn("[AuthenticationException] message={}", ex.getMessage(), ex);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponseDTO.error(errorCode, "인증 정보가 유효하지 않습니다."));
    }

    // ✅ OAuth2 인증 오류
    @ExceptionHandler(OAuth2AuthenticationException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleOAuth2Exception(OAuth2AuthenticationException ex) {
        ErrorCode errorCode = ErrorCode.AUTH_401_007;
        log.warn("[OAuth2AuthenticationException] message={}", ex.getMessage(), ex);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponseDTO.error(errorCode, "OAuth2 인증 중 오류가 발생했습니다."));
    }

    // ✅ 인가 오류
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorCode errorCode = ErrorCode.AUTH_403_002;
        log.warn("[AccessDeniedException] message={}", ex.getMessage(), ex);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponseDTO.error(errorCode, "접근 권한이 없습니다."));
    }

    // ✅ 잘못된 요청 파라미터 등
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorCode errorCode = ErrorCode.REQ_400_001;
        log.warn("[IllegalArgumentException] message={}", ex.getMessage(), ex);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponseDTO.error(errorCode, ex.getMessage()));
    }

    // ✅ 메일 전송 실패
    @ExceptionHandler(MailException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleMailException(MailException ex) {
        ErrorCode errorCode = ErrorCode.MAIL_500_001;
        log.error("[MailException] message={}", ex.getMessage(), ex);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponseDTO.error(errorCode, "메일 전송에 실패했습니다. 관리자에게 문의해주세요."));
    }

    // ✅ 파일 업로드 등 입출력 예외
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleIOException(IOException ex) {
        ErrorCode errorCode = ErrorCode.FILE_500_001;
        log.error("[IOException] {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponseDTO.error(errorCode, "파일 입출력 중 오류가 발생했습니다."));
    }

    // ✅ AWS S3 예외
    @ExceptionHandler(AmazonClientException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleAmazonException(AmazonClientException ex) {
        ErrorCode errorCode = ErrorCode.FILE_500_002;
        log.error("[AmazonS3 Error] {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponseDTO.error(errorCode, "S3 작업 처리 중 오류가 발생했습니다."));
    }

    // ✅ 알 수 없는 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleException(Exception ex) {
        ErrorCode errorCode = ErrorCode.ETC_520_001;
        log.error("[UnhandledException] message={}", ex.getMessage(), ex);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponseDTO.error(errorCode));
    }

    // ✅ WebRequest로부터 email 파라미터 추출
    private String extractEmailFromRequest(WebRequest request) {
        try {
            String email = request.getParameter("email");
            return (email != null && !email.isBlank()) ? email : null;
        } catch (Exception e) {
            log.warn("[extractEmailFromRequest] 이메일 파라미터 추출 실패", e);
            return null;
        }
    }
}