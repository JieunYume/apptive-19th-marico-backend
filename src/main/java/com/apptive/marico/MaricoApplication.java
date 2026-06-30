package com.apptive.marico;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MaricoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MaricoApplication.class, args);
	}

}
