package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		System.setProperty("spring.data.mongodb.uri", "mongodb+srv://prince21182_db_user:u1YTb1sqeBJQKSb6@cluster0.uz6uptp.mongodb.net/textile");
		SpringApplication.run(DemoApplication.class, args);
	}

}
