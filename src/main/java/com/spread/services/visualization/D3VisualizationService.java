package com.spread.services.visualization;

import java.nio.file.Files;
import java.nio.file.Path;

import com.spread.exceptions.SpreadException;

import org.springframework.stereotype.Service;

@Service
public class D3VisualizationService implements VisualizationService {

    private Path visualizationLocation;
    private Boolean isInit = false;

    @Override
    public void init(Path visualizationLocation) throws SpreadException {
        this.visualizationLocation = visualizationLocation;
        if(!visualizationExists()) {
            throw new SpreadException("VisualisationDirectoryNotFoundException", new String [][] {
                    {"directory", visualizationLocation.toString()}
                });
        }
        isInit = true;
    }

    /**
     * @return the isInit
     */
    @Override
    public Boolean isInitialized() {
        return isInit;
    }

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
