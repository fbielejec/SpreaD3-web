package com.spread.services.storage;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

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
    public void createRootDir() {
        try {
            Files.createDirectory(rootLocation);
        } catch (IOException e) {
            isInit = false;
            throw new StorageException("Could not initialize storage", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(MultipartFile file) {
        return exists(null, file);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(String subdirectory, MultipartFile file) {

        String filename = file.getOriginalFilename();

        try {

            Path path = (subdirectory == null) ? load(filename) : load(subdirectory, filename);
            Resource resource = new UrlResource(path.toUri());
            return resource.exists();

        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not resolve if file: " + filename + " exists.", e);
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
    public void store(MultipartFile file) {
        store(null, file);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void store(String subdirectory, MultipartFile file) throws StorageException {
        String filename = file.getOriginalFilename();
        try {

            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + filename);
            }

            if(subdirectory == null) {
                Files.copy(file.getInputStream(), rootLocation.resolve(filename));
            } else {
                Path childLocation = rootLocation.resolve(subdirectory);
                Files.copy(file.getInputStream(), childLocation.resolve(filename));
            }

        } catch (IOException e) {
            throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(String subdirectory, String filename, byte[] content) {
        try {

            Path location = null;
            if (subdirectory == null) {
                location = getRootLocation();
            } else {
                location = getSubdirectoryLocation(subdirectory);
            }

            Files.write(location.resolve(filename), content);

        } catch (IOException e) {
            throw new StorageException("Failed to write file " + filename, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(String filename, byte[] content) {
        write(null, filename, content);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copy(Path source) {
        copy (null, source);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copy(String subdirectory, Path source) {
        try {
            Path dest = null;
            if (subdirectory == null) {
                dest = getRootLocation();
            } else {
                dest = getSubdirectoryLocation(subdirectory);
            }
            FileUtils.copyDirectory(source.toFile(), dest.toFile());
        } catch (IOException e) {
            throw new StorageException("Failed to copy content from " + source.toString() + " into " + subdirectory, e);
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
    public Resource loadAsResource(String filename) {
        return loadAsResource(null, filename);
    }

    @Override
    public Resource loadAsResource(String subdirectory, String filename) {

        try {

            Path file = (subdirectory == null) ? load(filename) : load(subdirectory, filename);

            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);

            }

        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }

    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1).filter(path -> !path.equals(this.rootLocation))
                .map(path -> this.rootLocation.relativize(path));
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
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
    public void createSubdirectory(String subdirectory) {
        try {

            Path childLocation = rootLocation.resolve(subdirectory);
            Files.createDirectory(childLocation);

        } catch (IOException e) {
            throw new StorageException("Failed to create subdirectory: " + subdirectory, e);
        }
    }

    @Override
    public void deleteSubdirectory(String subdirectory) {
        Path childLocation = rootLocation.resolve(subdirectory);
        FileSystemUtils.deleteRecursively(childLocation.toFile());
    }

}
