package com.spread.services.visualization;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class D3VisualizationService implements VisualizationService {

    @Value("${spread.vis.location}")
    private String visualizationLocation;

    private Boolean isInit = false;
    
    @Override
    public void init() {
        if(!visualizationExists()) {
            throw new VisualisationDirectoryNotFoundException(visualizationLocation);
        }
        isInit = true;
    }
    
    /**
     * @return the isInit
     */
    public Boolean isInitialized() {
        return isInit;
    }

    @Override
    public Path getVisualisationDirectory() {
        if(!visualizationExists()) {
            throw new VisualisationDirectoryNotFoundException(visualizationLocation);
        }
        return Paths.get(visualizationLocation);
    }
    
    private boolean visualizationExists () {
        return Files.exists(Paths.get(visualizationLocation));
    }

}
