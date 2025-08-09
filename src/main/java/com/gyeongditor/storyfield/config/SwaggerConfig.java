package com.gyeongditor.storyfield.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@OpenAPIDefinition(servers = {
        @Server(url = "http://localhost:9080", description = "Local"),
        @Server(url = "https://api.storyfield.dev", description = "Prod")
})
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // JWT 보안 스키마
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI()
                .info(new Info()
                        .title("이야기밭 API 문서")
                        .description("이야기밭 서비스 백엔드 API 명세서입니다.")
                        .version("v1.0")
                        .contact(new Contact().name("StoryField Team").email("dev@storyfield.dev"))
                        .license(new License().name("MIT")))
                // 전역 태그 레지스트리  수정
                .tags(List.of(
                        new Tag().name("Auth").description("인증"),
                        new Tag().name("User").description("사용자"),
                        new Tag().name("Story").description("동화"),
                        new Tag().name("Image").description("이미지"),
                        new Tag().name("Health").description("헬스체크")
                ))
                .addSecurityItem(securityRequirement)
                .components(new Components().addSecuritySchemes("bearerAuth", bearerAuth));
    }

    // 경로 기반 그룹  수정
    @Bean public GroupedOpenApi allApis() {
        return GroupedOpenApi.builder()
                .group("all")
                .packagesToScan("com.gyeongditor.storyfield")
                .pathsToMatch("/**")
                .build();
    }

    // 전역 기본 응답 401 403 500 자동 부여  수정
    @Bean
    public OpenApiCustomizer defaultResponses() {
        return openApi -> openApi.getPaths().values().forEach(item ->
                item.readOperations().forEach(op -> {
                    var res401 = new ApiResponse().description("Unauthorized")
                            .content(new Content().addMediaType("application/json", new MediaType()
                                    .schema(new Schema<>().type("object"))));
                    var res403 = new ApiResponse().description("Forbidden")
                            .content(new Content().addMediaType("application/json", new MediaType()
                                    .schema(new Schema<>().type("object"))));
                    var res500 = new ApiResponse().description("Internal Server Error")
                            .content(new Content().addMediaType("application/json", new MediaType()
                                    .schema(new Schema<>().type("object"))));
                    op.getResponses().addApiResponse("401", res401);
                    op.getResponses().addApiResponse("403", res403);
                    op.getResponses().addApiResponse("500", res500);
                })
        );
    }
}
