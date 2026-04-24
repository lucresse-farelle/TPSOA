package com.tp.commandes;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ServiceCommandesApplication {
	public static void main(String[] args) {
		SpringApplication.run(ServiceCommandesApplication.class, args);
	}
}