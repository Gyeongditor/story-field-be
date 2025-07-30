package com.gyeongditor.storyfield;

import com.gyeongditor.storyfield.config.AwsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EntityScan("com.gyeongditor.storyfield.Entity") // 👈 꼭 확인
@EnableConfigurationProperties(AwsProperties.class)
public class StoryfieldApplication {

	public static void main(String[] args) {
		SpringApplication.run(StoryfieldApplication.class, args);
	}

}
