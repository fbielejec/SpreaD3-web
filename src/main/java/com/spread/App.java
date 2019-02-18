package com.spread;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.Executor;

import com.spread.controllers.ContinuousTreeController;
import com.spread.domain.KeyEntity;
import com.spread.loggers.AbstractLogger;
import com.spread.loggers.ILogger;
import com.spread.repositories.KeyRepository;
import com.spread.services.ipfs.IpfsService;
import com.spread.services.logging.LoggingService;
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

    @Value("${server.port}")
    private Integer port;

    @Value("${log.file.path}")
    private String logFilePath;

    @Value("${log.file.name}")
    private String logFileName;

    @Value("${app.logging.level}")
    private String appLoggingLevel;

    @Value("${sentry.logging.level}")
    private String sentryLoggingLevel;

    @Value("${spring.profiles.active}")
    private String activeProfile;

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
    private LoggingService loggingService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private KeyRepository keyRepository;

    @Autowired
    private IpfsService ipfsService;

    @Autowired
    private VisualizationService visualizationService;

    @Autowired
    private ContinuousTreeController continuousTreeController;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    CommandLineRunner init() {
        return (args) -> {

            HashMap<String, String> opts = new HashMap<String, String>() {
                    private static final long serialVersionUID = 1L;
                    {
                        put("stacktrace.app.packages", stackTraceAppPackages);
                    }};

            AbstractLogger logger = loggingService.init(activeProfile, sentryLoggingLevel, dsn, opts);

            keyRepository.save(new KeyEntity(secret));

            storageService.init(rootLocation);
            storageService.deleteAll();
            storageService.createRootDir();

            ipfsService.init(ipfsHost);
            visualizationService.init(visualizationLocation);

            continuousTreeController.init(logger);

            logger.log(ILogger.WARN, "Application rebooted!", new String[][] {
                    {"spring.profiles.active", activeProfile},
                    {"server.port", port.toString()},
                    {"secret", secret},
                    {"storage.location", rootLocation.toString()},
                    {"app.logging.level", appLoggingLevel},
                    {"log.file.path", logFilePath},
                    {"log.file.name", logFileName},
                    {"sentry.logging.level", sentryLoggingLevel},
                    {"sentry.dsn", dsn},
                    {"ipfs.host", ipfsHost},
                    {"spread.vis.location", visualizationLocation}

                });
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
