package com.bruceychen.tb001;

import org.springframework.boot.SpringApplication;

public class TestProjectTb001Application {

	public static void main(String[] args) {
		SpringApplication.from(ProjectTb001Application::main).with(TestcontainersConfiguration.class).run(args);
	}

}
