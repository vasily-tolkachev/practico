package com.myproject.practico;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ReminderApplication {
	public static void main(String[] args) {
		SpringApplication.run(ReminderApplication.class, args);
	}
}
