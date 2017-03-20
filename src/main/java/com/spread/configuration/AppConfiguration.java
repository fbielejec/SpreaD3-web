package com.spread.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.spread.services.ContinuousTreeServiceBean;
import com.spread.services.IContinuousTreeService;

@Configuration
public class AppConfiguration {

//	@Bean
//	public ModelMapper modelMapper() {
//		return new ModelMapper();
//	}

	@Bean
	public IContinuousTreeService continuousTreeService() {
		return new ContinuousTreeServiceBean();
	}

//	@Bean
//	public ContinuousTreeController continuousTreeController() {
//		return new ContinuousTreeController(new FileSystemStorageService(new StorageProperties()));
//	}

}
