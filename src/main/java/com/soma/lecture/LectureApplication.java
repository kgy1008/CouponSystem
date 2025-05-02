package com.soma.lecture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class LectureApplication {

	public static void main(String[] args) {
		SpringApplication.run(LectureApplication.class, args);
	}

}
