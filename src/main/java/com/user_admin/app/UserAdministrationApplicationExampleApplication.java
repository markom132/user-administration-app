package com.user_admin.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UserAdministrationApplicationExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserAdministrationApplicationExampleApplication.class, args);
	}

}
