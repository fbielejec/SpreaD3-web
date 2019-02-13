package com.spread.services.visualization;

public class VisualisationDirectoryNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 7749922443853264034L;
    
    public VisualisationDirectoryNotFoundException(String visualizationLocation) {
        super("Visualization directory" + visualizationLocation + " could not be found or does not exist");
    }
    
}

