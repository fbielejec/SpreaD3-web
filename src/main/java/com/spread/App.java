package com.spread;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.Executor;

import com.spread.domain.KeyEntity;
import com.spread.repositories.KeyRepository;
import com.spread.services.ipfs.IpfsService;
import com.spread.services.sentry.SentryLoggingService;
import com.spread.services.storage.StorageService;
import com.spread.services.visualization.VisualizationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@EnableAsync
public class App {

    @Value("${secret}")
    private String secret;

    @Value("${storage.location}")
    private Path rootLocation;

    @Value("${stacktrace.app.packages}")
    private String stackTraceAppPackages;

    @Value("${sentry.dsn}")
    private String dsn;

    @Value("${ipfs.host}")
    private String ipfsHost;

    @Value("${spread.vis.location}")
    private String visualizationLocation;

    @Autowired
    private StorageService storageService;

    @Autowired
    private KeyRepository keyRepository;

    @Autowired
    private IpfsService ipfsService;

    @Autowired
    private VisualizationService visualizationService;

    @Autowired
    SentryLoggingService sentry;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    CommandLineRunner init() {
        return (args) -> {

            HashMap<String, String> opts = new HashMap<>();
            opts.put("stacktrace.app.packages", stackTraceAppPackages);
            // sentry.init(dsn, opts);

            keyRepository.save(new KeyEntity(secret));

            storageService.init(rootLocation);
            storageService.deleteAll();
            storageService.createRootDir();

            ipfsService.init(ipfsHost);
            visualizationService.init(visualizationLocation);
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
