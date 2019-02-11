package com.spread.services.ipfs;

import java.io.IOException;
import java.nio.file.Path;

public interface IpfsService {

    void init();

    Boolean isInitialized();
    
    String addDirectory(Path directory) throws IOException;
    
}
