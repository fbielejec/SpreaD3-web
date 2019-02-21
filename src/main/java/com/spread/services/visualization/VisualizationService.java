package com.spread.services.visualization;

import java.nio.file.Path;

import com.spread.exceptions.SpreadException;

public interface VisualizationService {

    public void init(Path visualizationLocation) throws SpreadException;

    public Boolean isInitialized();

    public Path getVisualisationDirectory() throws SpreadException;

}
