package com.gyeongditor.storyfield.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // JWT Security 스키마 정의
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        // SecurityRequirement: 전역에 Authorization 필요하도록 설정
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI()
                .info(new Info()
                        .title("이야기밭 API 문서")
                        .description("이야기밭 서비스 백엔드 API 명세서입니다.")
                        .version("v1.0"))
                .addSecurityItem(securityRequirement)
                .components(new Components().addSecuritySchemes("bearerAuth", bearerAuth));
    }
}
