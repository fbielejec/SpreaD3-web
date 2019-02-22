package com.spread.services.ipfs;

import java.io.IOException;
import java.nio.file.Path;

public interface IpfsService {

    void init(String multiaddress);

    void init(String host, Integer port);

    Boolean isInitialized();

    String addDirectory(Path directory) throws IOException;

}
