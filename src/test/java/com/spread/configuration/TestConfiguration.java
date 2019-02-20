package com.spread.configuration;

import com.spread.services.storage.StorageService;
import com.spread.services.visualization.VisualizationService;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
public class TestConfiguration {

    @Bean
    @Primary
    public StorageService storageService() {
        return Mockito.mock(StorageService.class);
    }

    @Bean
    @Primary
    public VisualizationService visualizationServiceTest() {
        return Mockito.mock(VisualizationService.class);
    }

}
