package com.gyeongditor.storyfield.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.core.task.TaskExecutor;

/**
 * 비동기 처리를 위한 설정 클래스
 * 스토리 이미지 업로드 병렬 처리용 ThreadPool 설정
 */
@Configuration
@EnableAsync // @EnableAsync 활성화
public class AsyncConfig {

    /**
     * 스토리 이미지 업로드용 TaskExecutor Bean 등록
     *
     * @return storyImageTaskExecutor 스레드풀
     */
    @Bean(name = "storyImageTaskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 스레드풀 파라미터 설정 (트래픽 상황에 따라 추후 조정 가능)
        executor.setCorePoolSize(4);              // 기본 스레드 4개
        executor.setMaxPoolSize(10);              // 최대 스레드 10개
        executor.setQueueCapacity(50);            // 큐 크기 50
        executor.setThreadNamePrefix("StoryImage-"); // 스레드 이름 접두사

        // 애플리케이션 종료 시 설정
        executor.setWaitForTasksToCompleteOnShutdown(true); // 진행 중인 작업 완료 대기
        executor.setAwaitTerminationSeconds(30);            // 최대 30초 대기

        executor.initialize();
        return executor;
    }
}
