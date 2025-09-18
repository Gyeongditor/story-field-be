package com.gyeongditor.storyfield.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

@Configuration
public class FastApiClientConfig {

    @Bean
    @ConfigurationProperties(prefix = "fastapi")
    public FastApiProps fastApiProps() {
        return new FastApiProps();
    }

    @Bean
    public WebClient fastApiWebClient(FastApiProps props) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, props.getConnectTimeoutMs())
                .responseTimeout(Duration.ofMillis(props.getReadTimeoutMs()))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(props.getReadTimeoutMs(), TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(props.getWriteTimeoutMs(), TimeUnit.MILLISECONDS))
                );

        return WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    // **** 보안 로그 마스킹 처리
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(req ->
                Mono.just(ClientRequest.from(req)
                        .headers(h -> {
                            if (h.containsKey(HttpHeaders.AUTHORIZATION)) {
                                h.set(HttpHeaders.AUTHORIZATION, "Bearer ****");
                            }
                        }).build()));
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(Mono::just);
    }

    @Data
    public static class FastApiProps {
        private String baseUrl;
        private Integer connectTimeoutMs = 1000;
        private Integer readTimeoutMs = 5000;
        private Integer writeTimeoutMs = 3000;
        private Integer requestTimeoutMs = 6000;
        private String serviceBearer;
    }
}
