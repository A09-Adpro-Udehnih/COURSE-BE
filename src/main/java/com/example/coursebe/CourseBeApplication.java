package com.example.coursebe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CourseBeApplication {

	public static void main(String[] args) {
		System.out.println("Running DB migrations...");
        MigrationManager.migrate();
        System.out.println("Done!");
		SpringApplication.run(CourseBeApplication.class, args);
	}

}
