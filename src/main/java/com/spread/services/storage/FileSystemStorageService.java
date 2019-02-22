package com.spread.services.storage;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import com.spread.exceptions.SpreadException;
import com.spread.loggers.AbstractLogger;
import com.spread.loggers.ILogger;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService implements StorageService {

    private Boolean isInit = false;
    private Path rootLocation;
    private AbstractLogger logger;

    @Override
    public void init(Path rootLocation, AbstractLogger logger) {

        this.rootLocation = rootLocation;
        this.logger = logger;

        logger.log(ILogger.INFO, "Initialized storage service", new String[][]{
                {"rootLocation", rootLocation.toString()}
            });

        isInit = true;
    }

    @Override
    public void createRootDir() throws SpreadException {
        try {
            Files.createDirectory(rootLocation);
        } catch (IOException e) {
            isInit = false;
            throw new SpreadException(SpreadException.Type.STORAGE_EXCEPTION,
                                      "Could not create root directory: " + e.getMessage(),
                                      new String[][] {
                                          {"reason", e.getMessage()}
                                      });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(MultipartFile file) throws SpreadException {
        return exists(null, file);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(String subdirectory, MultipartFile file) throws SpreadException {
        String filename = file.getOriginalFilename();
        try {
            Path path = (subdirectory == null) ? load(filename) : load(subdirectory, filename);
            Resource resource = new UrlResource(path.toUri());
            return resource.exists();
        } catch (MalformedURLException e) {
            throw new SpreadException(SpreadException.Type.STORAGE_EXCEPTION,
                                      "Could not resolve if file: " + filename + " exists.",
                                      new String[][] {
                                          {"reason", e.getMessage()}
                                      });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(Path location) {
        File directory = location.toFile();
        return directory.exists();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void store(MultipartFile file) throws SpreadException {
        store(null, file);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void store(String subdirectory, MultipartFile file) throws SpreadException {
        String filename = file.getOriginalFilename();
        try {

            if (file.isEmpty()) {
                throw new SpreadException(SpreadException.Type.STORAGE_EXCEPTION, "Failed to store empty file " + filename);
            }

            if(subdirectory == null) {
                Files.copy(file.getInputStream(), rootLocation.resolve(filename));
            } else {
                Path childLocation = rootLocation.resolve(subdirectory);
                Files.copy(file.getInputStream(), childLocation.resolve(filename));
            }

        } catch (IOException e) {
            String message = Optional.ofNullable(e.getMessage()).orElse("null");
            throw new SpreadException(SpreadException.Type.STORAGE_EXCEPTION,
                                      "Failed to store file " + file.getOriginalFilename(),
                                      new String[][] {
                                          {"reason", message}
                                      });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(String subdirectory, String filename, byte[] content) throws SpreadException {
        try {

            Path location = null;
            if (subdirectory == null) {
                location = getRootLocation();
            } else {
                location = getSubdirectoryLocation(subdirectory);
            }

            Files.write(location.resolve(filename), content);
        } catch (IOException e) {
            String message = Optional.ofNullable(e.getMessage()).orElse("null");
            throw new SpreadException(SpreadException.Type.STORAGE_EXCEPTION,
                                      "Failed to write file " + filename,
                                      new String[][] {
                                          {"reason", message}
                                      });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(String filename, byte[] content) throws SpreadException {
        write(null, filename, content);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copy(Path source) throws SpreadException {
        copy (null, source);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copy(String subdirectory, Path source) throws SpreadException {
        try {
            Path dest = null;
            if (subdirectory == null) {
                dest = getRootLocation();
            } else {
                dest = getSubdirectoryLocation(subdirectory);
            }
            FileUtils.copyDirectory(source.toFile(), dest.toFile());
        } catch (IOException e) {
            String message = Optional.ofNullable(e.getMessage()).orElse("null");
            throw new SpreadException(SpreadException.Type.STORAGE_EXCEPTION,
                                      "Failed to copy content from " + source.toString() + " into " + subdirectory,
                                      new String[][] {
                                          {"reason", message}
                                      });
        }
    }

    @Override
    public Path load(String filename) {
        return load(null, filename);
    }

    @Override
    public Path load(String subdirectory, String filename) {

        if(subdirectory == null) {
            return rootLocation.resolve(filename);
        }

        Path childLocation = rootLocation.resolve(subdirectory);
        return childLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) throws SpreadException {
        return loadAsResource(null, filename);
    }

    @Override
    public Resource loadAsResource(String subdirectory, String filename) throws SpreadException {

        try {

            Path file = (subdirectory == null) ? load(filename) : load(subdirectory, filename);

            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new SpreadException(SpreadException.Type.STORAGE_EXCEPTION,
                                          "Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            String message = Optional.ofNullable(e.getMessage()).orElse("null");
            throw new SpreadException(SpreadException.Type.STORAGE_EXCEPTION,
                                      "Could not read file: " + filename,
                                      new String[][] {
                                          {"reason", message}
                                      });
        }
    }

    @Override
    public Stream<Path> loadAll() throws SpreadException {
        try {
            return Files.walk(this.rootLocation, 1).filter(path -> !path.equals(this.rootLocation))
                .map(path -> this.rootLocation.relativize(path));
        } catch (IOException e) {
            String message = Optional.ofNullable(e.getMessage()).orElse("null");
            throw new SpreadException(SpreadException.Type.STORAGE_EXCEPTION,
                                      "Failed to store files ",
                                      new String[][] {
                    {"reason", message}
                });
        }
    }

    @Override
    public void delete(String filename) {
        delete(null, filename);
    }

    @Override
    public void delete(String subdirectory, String filename) {
        Path path = (subdirectory == null) ? load(filename) : load(subdirectory, filename);
        FileSystemUtils.deleteRecursively(path.toFile());
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public Boolean isInitialized() {
        return isInit;
    }

    @Override
    public Path getRootLocation() {
        return rootLocation;
    }

    @Override
    public Path getSubdirectoryLocation(String subdirectory) {
        return rootLocation.resolve(subdirectory);
    }

    @Override
    public void createSubdirectory(String subdirectory) throws SpreadException {
        try {
            Path childLocation = rootLocation.resolve(subdirectory);
            Files.createDirectory(childLocation);
        } catch (IOException e) {
            String message = Optional.ofNullable(e.getMessage()).orElse("null");
            throw new SpreadException(SpreadException.Type.STORAGE_EXCEPTION,
                                      "Failed to create subdirectory: " + subdirectory,
                                      new String[][] {
                                          {"reason", message}
                                      });
        }
    }

    @Override
    public void deleteSubdirectory(String subdirectory) {
        Path childLocation = rootLocation.resolve(subdirectory);
        FileSystemUtils.deleteRecursively(childLocation.toFile());
    }

}
