package com.gyeongditor.storyfield.swagger;

import com.gyeongditor.storyfield.response.ErrorCode;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerErrorResponseConfig {

    @Bean
    public OperationCustomizer customizeErrorExamples() {
        return (operation, handlerMethod) -> {
            ApiErrorExample annotation = handlerMethod.getMethodAnnotation(ApiErrorExample.class);

            if (annotation != null) {
                ApiResponses responses = operation.getResponses();

                for (ErrorCode errorCode : annotation.value()) {
                    ApiResponse apiResponse = new ApiResponse()
                            .description(errorCode.getMessage());

                    // JSON 예시 자동 생성
                    String exampleJson = String.format("""
                        {
                          "status": %d,
                          "code": "%s",
                          "message": "%s",
                          "data": null
                        }
                        """, errorCode.getStatus().value(), errorCode.getCode(), errorCode.getMessage());

                    Example example = new Example().value(exampleJson);

                    Content content = new Content()
                            .addMediaType("application/json",
                                    new MediaType().addExamples("example", example));

                    apiResponse.setContent(content);

                    responses.addApiResponse(
                            String.valueOf(errorCode.getStatus().value()),
                            apiResponse
                    );
                }
            }

            return operation;
        };
    }
}
