package com.onboardguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class OnboardguardBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnboardguardBackendApplication.class, args);
	}

}
