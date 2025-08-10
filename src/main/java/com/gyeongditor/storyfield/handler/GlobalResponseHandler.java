package com.gyeongditor.storyfield.handler;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.exception.CustomException;
import com.gyeongditor.storyfield.handler.mapper.SuccessCodeMapper;
import com.gyeongditor.storyfield.handler.mapper.*;
import com.gyeongditor.storyfield.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.io.IOException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    private final SuccessCodeMapper successCodeMapper;

    // (기존) 에러 매퍼들 …
    private final AuthErrorMapper authErrorMapper;
    private final OAuth2ErrorMapper oAuth2ErrorMapper;
    private final RequestErrorMapper requestErrorMapper;
    private final FileErrorMapper fileErrorMapper;
    private final MailErrorMapper mailErrorMapper;
    private final ResourceErrorMapper resourceErrorMapper;
    private final ServerErrorMapper serverErrorMapper;
    private final PolicyErrorMapper policyErrorMapper;
    private final BusinessErrorMapper businessErrorMapper;
    private final FallbackErrorMapper fallbackErrorMapper;

    // ====== 성공 응답 래핑 ======
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return !returnType.getParameterType().equals(ResponseEntity.class);
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  org.springframework.http.server.ServerHttpRequest request,
                                  org.springframework.http.server.ServerHttpResponse response) {

        if (body instanceof ApiResponseDTO) return body;

        HttpMethod method = request.getMethod(); // null 가능성 체크 필요
        String path = request.getURI().getPath();

        HttpStatus statusByMethod;
        if (method == HttpMethod.POST)       statusByMethod = HttpStatus.CREATED;
        else if (method == HttpMethod.DELETE) statusByMethod = HttpStatus.NO_CONTENT;
        else                                  statusByMethod = HttpStatus.OK;

        // 도메인 성공코드 우선 매핑
        SuccessCode success = successCodeMapper.resolve(path, method, statusByMethod);

        response.setStatusCode(success.getStatus());
        if (success.getStatus() == HttpStatus.NO_CONTENT) return null;

        return ApiResponseDTO.success(success, body);
    }

    // ====== 에러 응답 (중략: 이전에 분리해둔 매퍼 기반 처리 그대로 유지) ======

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponseDTO<Object>> onCustom(CustomException ex) {
        var code = ex.getErrorCode();
        log.warn("[CustomException] {} - {}", code.getCode(), ex.getCustomMessage());
        return ResponseEntity.status(code.getStatus()).body(ApiResponseDTO.error(code, ex.getCustomMessage()));
    }

    @ExceptionHandler({ AuthenticationException.class, AccessDeniedException.class })
    public ResponseEntity<ApiResponseDTO<Object>> onAuth(Exception ex) {
        var mapped = authErrorMapper.map(ex);
        log.warn("[Auth] {} - {}", mapped.code().getCode(), mapped.message(), ex);
        return ResponseEntity.status(mapped.code().getStatus()).body(ApiResponseDTO.error(mapped.code(), mapped.message()));
    }

    @ExceptionHandler(OAuth2AuthenticationException.class)
    public ResponseEntity<ApiResponseDTO<Object>> onOAuth2(OAuth2AuthenticationException ex) {
        var mapped = oAuth2ErrorMapper.map(ex);
        log.warn("[OAuth2] {} - {}", mapped.code().getCode(), mapped.message(), ex);
        return ResponseEntity.status(mapped.code().getStatus()).body(ApiResponseDTO.error(mapped.code(), mapped.message()));
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class, BindException.class,
            MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class,
            HttpRequestMethodNotSupportedException.class, HttpMediaTypeNotSupportedException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiResponseDTO<Object>> onRequest(Exception ex) {
        var mapped = requestErrorMapper.map(ex);
        log.warn("[Request] {} - {}", mapped.code().getCode(), mapped.message(), ex);
        return ResponseEntity.status(mapped.code().getStatus()).body(ApiResponseDTO.error(mapped.code(), mapped.message()));
    }

    @ExceptionHandler({ IOException.class, com.amazonaws.AmazonClientException.class })
    public ResponseEntity<ApiResponseDTO<Object>> onFile(Exception ex) {
        var mapped = fileErrorMapper.map(ex);
        log.error("[File] {} - {}", mapped.code().getCode(), mapped.message(), ex);
        return ResponseEntity.status(mapped.code().getStatus()).body(ApiResponseDTO.error(mapped.code(), mapped.message()));
    }

    @ExceptionHandler(org.springframework.mail.MailException.class)
    public ResponseEntity<ApiResponseDTO<Object>> onMail(org.springframework.mail.MailException ex) {
        var mapped = mailErrorMapper.map(ex);
        log.error("[Mail] {} - {}", mapped.code().getCode(), mapped.message(), ex);
        return ResponseEntity.status(mapped.code().getStatus()).body(ApiResponseDTO.error(mapped.code(), mapped.message()));
    }

    @ExceptionHandler({
            jakarta.persistence.EntityNotFoundException.class,
            org.springframework.dao.OptimisticLockingFailureException.class,
            org.springframework.dao.DataIntegrityViolationException.class
    })
    public ResponseEntity<ApiResponseDTO<Object>> onResource(Exception ex) {
        var mapped = resourceErrorMapper.map(ex);
        log.warn("[Resource] {} - {}", mapped.code().getCode(), mapped.message(), ex);
        return ResponseEntity.status(mapped.code().getStatus()).body(ApiResponseDTO.error(mapped.code(), mapped.message()));
    }

    @ExceptionHandler({
            org.springframework.dao.DataAccessResourceFailureException.class,
            org.springframework.web.client.RestClientException.class,
            org.springframework.web.server.ResponseStatusException.class,
            java.util.concurrent.TimeoutException.class
    })
    public ResponseEntity<ApiResponseDTO<Object>> onServerInfra(Exception ex) {
        var mapped = serverErrorMapper.map(ex);
        log.error("[Server/Infra] {} - {}", mapped.code().getCode(), mapped.message(), ex);
        return ResponseEntity.status(mapped.code().getStatus()).body(ApiResponseDTO.error(mapped.code(), mapped.message()));
    }

    @ExceptionHandler(org.springframework.security.web.csrf.CsrfException.class)
    public ResponseEntity<ApiResponseDTO<Object>> onPolicy(Exception ex) {
        var mapped = policyErrorMapper.map(ex);
        log.warn("[Policy] {} - {}", mapped.code().getCode(), mapped.message(), ex);
        return ResponseEntity.status(mapped.code().getStatus()).body(ApiResponseDTO.error(mapped.code(), mapped.message()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Object>> onUnhandled(Exception ex) {
        var mapped = fallbackErrorMapper.map(ex);
        log.error("[Unhandled] {} - {}", mapped.code().getCode(), mapped.message(), ex);
        return ResponseEntity.status(mapped.code().getStatus()).body(ApiResponseDTO.error(mapped.code(), mapped.message()));
    }
}
