package com.gyeongditor.storyfield.swagger.config;


import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

@Component
public class RemoveDefault200ResponseCustomizer implements OperationCustomizer {

    @Override
    public io.swagger.v3.oas.models.Operation customize(
            io.swagger.v3.oas.models.Operation operation,
            HandlerMethod handlerMethod) {

        ApiResponses responses = operation.getResponses();
        if (responses != null && responses.containsKey("200")) {
            // 불필요한 기본 200 응답 제거
            responses.remove("200");
        }

        return operation;
    }
}
