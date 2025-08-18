package com.gyeongditor.storyfield.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    @ConfigurationProperties(prefix = "cors")
    public CorsProps corsProps() {
        return new CorsProps();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsFilter corsFilter(CorsProps props) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(props.getAllowedOrigins());
        config.setAllowedMethods(props.getAllowedMethods());
        config.setAllowedHeaders(props.getAllowedHeaders());
        config.setExposedHeaders(props.getExposedHeaders());
        config.setAllowCredentials(true);
        config.setMaxAge(props.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // ✅ RN 전용 엔드포인트에만 CORS 적용
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }

    @Data
    public static class CorsProps {
        private List<String> allowedOrigins;
        private List<String> allowedMethods;
        private List<String> allowedHeaders;
        private List<String> exposedHeaders;
        private Long maxAge;
    }
}
