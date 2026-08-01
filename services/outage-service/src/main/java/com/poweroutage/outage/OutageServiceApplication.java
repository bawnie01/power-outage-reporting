package com.poweroutage.outage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OutageServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OutageServiceApplication.class, args);
	}

}
