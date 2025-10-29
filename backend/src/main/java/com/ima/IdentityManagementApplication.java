package com.ima;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class IdentityManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(IdentityManagementApplication.class, args);
	}

}
