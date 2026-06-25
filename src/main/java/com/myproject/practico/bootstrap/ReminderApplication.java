package com.myproject.practico.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.myproject.practico")
public class ReminderApplication {
	static void main(String[] args) {
		SpringApplication.run(ReminderApplication.class, args);
	}
}