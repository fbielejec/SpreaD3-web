package com.spread;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.spread.domain.KeyEntity;
import com.spread.repositories.KeyRepository;
import com.spread.services.storage.StorageService;

@SpringBootApplication
public class App {

	@Value("${secret}")
	private String secret;

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	@Bean
	CommandLineRunner init(StorageService storageService, KeyRepository keyRepository) {
		return (args) -> {
			keyRepository.save(new KeyEntity(secret));
			storageService.deleteAll();
			storageService.init();
		};
	}

}