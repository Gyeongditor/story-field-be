package com.gyeongditor.storyfield.swagger.config;

import com.gyeongditor.storyfield.response.SuccessCode;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

@Component
public class ApiSuccessExampleProcessor implements OperationCustomizer {

    @Override
    public io.swagger.v3.oas.models.Operation customize(io.swagger.v3.oas.models.Operation operation, HandlerMethod handlerMethod) {
        ApiSuccessResponse annotation = handlerMethod.getMethodAnnotation(ApiSuccessResponse.class);
        if (annotation != null) {
            for (SuccessCode successCode : annotation.value()) {
                // 응답 스키마 + 예제 동시 등록
                ApiResponse response = new ApiResponse()
                        .description(successCode.getMessage())
                        .content(new Content().addMediaType("application/json",
                                new MediaType()
                                        .schema(new Schema<>().type("object")) // 스키마 명시
                                        .addExamples(successCode.name(),
                                                new Example().value(buildExample(successCode)))
                        ));

                operation.getResponses().addApiResponse(
                        String.valueOf(successCode.getStatus().value()), response
                );
            }
        }
        return operation;
    }

    private String buildExample(SuccessCode successCode) {
        return """
               {
                 "status": %d,
                 "code": "%s",
                 "message": "%s",
                 "data": null
               }
               """.formatted(
                successCode.getStatus().value(),
                successCode.getCode(),
                successCode.getMessage()
        );
    }
}
