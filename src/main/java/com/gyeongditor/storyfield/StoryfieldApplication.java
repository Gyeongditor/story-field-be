package com.gyeongditor.storyfield;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("com.gyeongditor.storyfield.Entity") // 👈 꼭 확인
public class StoryfieldApplication {

	public static void main(String[] args) {
		SpringApplication.run(StoryfieldApplication.class, args);
	}

}
