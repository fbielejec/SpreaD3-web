package com.spread.configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.spread.exceptions.SpreadException;
import com.spread.loggers.AbstractLogger;
import com.spread.loggers.DefaultLogger;
import com.spread.services.storage.FileSystemStorageService;
import com.spread.services.storage.StorageService;
import com.spread.services.visualization.D3VisualizationService;
import com.spread.services.visualization.VisualizationService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
public class TestConfiguration {

    @Value("${storage.location}")
    private Path tmpRootLocation;

    private AbstractLogger logger = new DefaultLogger();

    @Bean
    @Primary
    public StorageService storageService() throws IOException {
        Path path = Files.createTempDirectory("temp");
        StorageService storageService = new FileSystemStorageService();
        storageService.init(path, logger);
        return storageService;
    }

    @Bean
    @Primary
    public VisualizationService visualizationServiceTest() throws SpreadException {

        Path visualizationLocation = Paths.get(getClass().getClassLoader().getResource("spread-vis").getPath());

        VisualizationService visualizationService = new D3VisualizationService();
        visualizationService.init(visualizationLocation);

        // return Mockito.mock(VisualizationService.class);
        return visualizationService;
    }

}
