package com.gyeongditor.storyfield.config;


import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FastApiClient {

    private final WebClient fastApiWebClient;

    public String ping() {
        return fastApiWebClient.get()
                .uri("/ping")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String saveStory(String saveStoryDtoString,
                            MultipartFile thumbnail,
                            List<MultipartFile> pageImages) {
        return fastApiWebClient.post()
                .uri("/stories/save")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("saveStoryDTO", saveStoryDtoString)
                        .with("thumbnail", thumbnail.getResource())
                        .with("pageImages", pageImages.stream().map(MultipartFile::getResource).toList())
                )
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
