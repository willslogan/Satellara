package com.satellaratech.satellara;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SatellaraApplication {

	public static void main(String[] args) {
		SpringApplication.run(SatellaraApplication.class, args);
	}

}
