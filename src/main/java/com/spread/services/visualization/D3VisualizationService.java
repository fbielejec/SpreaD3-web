package com.spread.services.visualization;

import java.nio.file.Files;
import java.nio.file.Path;

import com.spread.exceptions.SpreadException;
import com.spread.loggers.AbstractLogger;
import com.spread.loggers.ILogger;

import org.springframework.stereotype.Service;

@Service
public class D3VisualizationService implements VisualizationService {

    private Path visualizationLocation;
    private Boolean isInit = false;
    private AbstractLogger logger;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Path visualizationLocation, AbstractLogger logger) throws SpreadException {
        this.visualizationLocation = visualizationLocation;
        this.logger = logger;
        if(!visualizationExists()) {
            throw new SpreadException("VisualisationDirectoryNotFoundException", new String [][] {
                    {"directory", visualizationLocation.toString()}
                });
        }

        logger.log(ILogger.INFO, "Initialized storage service", new String[][]{
                {"visualizationLocation", visualizationLocation.toString()}
            });

        isInit = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean isInitialized() {
        return isInit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getVisualisationDirectory() throws SpreadException {
        if(!visualizationExists()) {
            throw new SpreadException("VisualisationDirectoryNotFoundException", new String [][]{
                    {"directory", visualizationLocation.toString()}
                });
        }
        return visualizationLocation;
    }

    private boolean visualizationExists () {
        return Files.exists(visualizationLocation);
    }

}
