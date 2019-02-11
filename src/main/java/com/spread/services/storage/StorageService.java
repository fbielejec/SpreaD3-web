package com.spread.services.storage;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    void init();

    Boolean isInitialized();
    
    boolean exists(MultipartFile file);
    
    boolean exists(String subdirectory, MultipartFile file);
    
    void store(MultipartFile file) throws StorageException;

    void store(String subdirectory, MultipartFile file) throws StorageException;
    
    Path load(String filename);

    Path load(String subdirectory, String filename);
    
    Resource loadAsResource(String filename);

    Resource loadAsResource(String subdirectory, String filename);
    
    void delete(String filename);

    void delete(String subdirectory, String filename);
    
    void deleteAll();

    Path getRootLocation();

    Path getSubdirectoryLocation(String subdirectory);
    
    Stream<Path> loadAll();

    void createSubdirectory(String subdirectory);

    void deleteSubdirectory(String subdirectory);
	
    boolean directoryExists(Path location);
    
}
