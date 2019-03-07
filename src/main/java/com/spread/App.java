package com.spread;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.Executor;

import com.spread.controllers.ContinuousTreeController;
import com.spread.controllers.DiscreteTreeController;
import com.spread.controllers.TokenController;
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
    private Long port;

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

    @Value("${ipfs.port}")
    private Long ipfsPort;

    @Value("${spread.vis.location}")
    private Path visualizationLocation;

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

    @Autowired
    private DiscreteTreeController discreteTreeController;

    @Autowired
    private TokenController tokenController;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    CommandLineRunner init() {
        return (args) -> {

            AbstractLogger logger = loggingService.init(activeProfile, sentryLoggingLevel, dsn, new HashMap<String, String>() {
                    private static final long serialVersionUID = 1L;
                    {
                        put("stacktrace.app.packages", stackTraceAppPackages);
                    }});

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
                    {"ipfs.port", ipfsPort == null ? "null" : ipfsPort.toString()},
                    {"spread.vis.location", visualizationLocation.toString()}

                });

            if(!storageService.isInitialized()) {
                storageService.init(rootLocation, logger);
                storageService.deleteAll();

                if(!storageService.exists(rootLocation))
                    storageService.createRootDir();
            }

            if(!visualizationService.isInitialized()) {
                visualizationService.init(visualizationLocation, logger);
            }

            keyRepository.save(new KeyEntity(secret));
            ipfsService.init(ipfsHost, ipfsPort.intValue());
            continuousTreeController.init(logger);
            discreteTreeController.init(logger);
            tokenController.init(logger);
        };
    }

    @Bean(name = "longRunningTaskExecutor")
    public Executor longRunningTaskExecutor() {
        return newTaskExecutor("spread-long-", 2);
    }

    private Executor newTaskExecutor(final String threadNamePrefix, int corePoolSize) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(corePoolSize);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.initialize();
        return executor;
    }

}
