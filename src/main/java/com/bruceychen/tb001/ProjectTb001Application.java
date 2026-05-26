package com.bruceychen.tb001;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProjectTb001Application {

	public static void main(String[] args) {
		SpringApplication.run(ProjectTb001Application.class, args);
	}

}
