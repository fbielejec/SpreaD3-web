package com.spread.services.visualization;

import java.nio.file.Path;

import com.spread.exceptions.SpreadException;
import com.spread.loggers.AbstractLogger;

public interface VisualizationService {

    public void init(Path visualizationLocation, AbstractLogger logger) throws SpreadException;

    /**
     * @return the isInit
     */
    public Boolean isInitialized();

    public Path getVisualisationDirectory() throws SpreadException;

}
