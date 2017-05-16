package com.spread.services.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

    void init();

    Boolean isInitialized();
    
    boolean exists(MultipartFile file);
    
    void store(MultipartFile file) throws StorageException;

    Stream<Path> loadAll();

    Path load(String filename);

    Resource loadAsResource(String filename);

    void delete(String filename);
    
    void deleteAll();

    Path getRootLocation();
    
}
