package com.spread.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.spread.controller.bean.IContinuousTreeService;
import com.spread.controller.bean.impl.ContinuousTreeServiceBean;

@Configuration
public class AppConfig {

	@Bean(name = "modelMapper")
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	@Bean(name = "continuousTreeService")
	public IContinuousTreeService continuousTreeService() {
		return new ContinuousTreeServiceBean();
	}

//	@Bean(name = "continuousTreeModelDAO")
//	public IContinuousTreeModelDAO continuousTreeModelDAO() {
//		return new ContinuousTreeModelDAO();
//	}

}
