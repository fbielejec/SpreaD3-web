package com.spread;

import java.util.concurrent.Executor;

import com.spread.domain.KeyEntity;
import com.spread.repositories.KeyRepository;
import com.spread.services.ipfs.IpfsService;
import com.spread.services.storage.StorageService;
import com.spread.services.visualization.VisualizationService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import io.sentry.Sentry;

@SpringBootApplication
@EnableAsync
public class App {

    @Value("${secret}")
    private String secret;

    @Value("${sentry.dsn}")
    private String sentryDsn;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    CommandLineRunner init(StorageService storageService,
                           KeyRepository keyRepository,
                           IpfsService ipfsService,
                           VisualizationService visualizationService) {
        return (args) -> {
            Sentry.init(sentryDsn);
            keyRepository.save(new KeyEntity(secret));
            storageService.deleteAll();
            storageService.init();
            ipfsService.init();
            visualizationService.init();
        };
    }

    @Bean(name = "threadPoolTaskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("spread-task-");
        executor.initialize();
        return executor;
    }

}
