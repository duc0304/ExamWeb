package com.killian.SpringBoot.ExamApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication (exclude =  SecurityAutoConfiguration.class)
@ComponentScan("com.killian.SpringBoot")
@EntityScan(basePackages = "com.killian.SpringBoot.ExamApp.models")
@EnableScheduling
public class SpringBootDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootDemoApplication.class, args);
	}	
}
