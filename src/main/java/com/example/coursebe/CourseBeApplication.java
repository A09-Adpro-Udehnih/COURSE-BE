package com.example.coursebe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class CourseBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(CourseBeApplication.class, args);
	}
}