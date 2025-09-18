package com.gyeongditor.storyfield.swagger.config;

import com.gyeongditor.storyfield.response.ErrorCode;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

@Component
public class ApiErrorExampleProcessor implements OperationCustomizer {

    @Override
    public io.swagger.v3.oas.models.Operation customize(
            io.swagger.v3.oas.models.Operation operation,
            HandlerMethod handlerMethod
    ) {
        ApiErrorResponse annotation = handlerMethod.getMethodAnnotation(ApiErrorResponse.class);

        if (annotation != null) {
            for (ErrorCode errorCode : annotation.value()) {
                // JSON 예시 자동 생성
                String exampleJson = """
                    {
                      "status": %d,
                      "code": "%s",
                      "message": "%s",
                      "data": null
                    }
                    """.formatted(
                        errorCode.getStatus().value(),
                        errorCode.getCode(),
                        errorCode.getMessage()
                );

                Example example = new Example().value(exampleJson);

                ApiResponse apiResponse = new ApiResponse()
                        .description(errorCode.getMessage())
                        .content(new Content().addMediaType(
                                "application/json",
                                new MediaType().addExamples(errorCode.getCode(), example)
                        ));

                operation.getResponses().addApiResponse(
                        String.valueOf(errorCode.getStatus().value()),
                        apiResponse
                );
            }
        }

        return operation;
    }
}
