package com.spread.services.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    void init();

    Boolean isInitialized();

    /**
     * Predicate whether a given file exist in a root directory
     *
     * @param  file to check
     * @return boolean value.
     */
    boolean exists(MultipartFile file);

    /**
     * Predicate whether a given file exist in a subdirectory
     *
     * @param  subdirectory : location to check
     * @param  file to check for
     * @return boolean value.
     */
    boolean exists(String subdirectory, MultipartFile file);

    /**
     * Predicate whether a given directory exist
     *
     * @param  location to check
     * @return boolean value.
     */
    boolean exists(Path location);

    Path getRootLocation();

    Path getSubdirectoryLocation(String subdirectory);

    /**
     * stores a file in the root directory
     *
     * @param file : file to store
     */
    void store(MultipartFile file) throws StorageException;

    /**
     * stores a file in the subdirectory
     *
     * @param subdirectory : subdir to store the file in
     * @param file : file to store
     */
    void store(String subdirectory, MultipartFile file) throws StorageException;

    /**
     * Writes a content to a file in the root directory
     *
     * @param filename : filename to write to
     * @param content : array of bytes to write
     */
    void write(String filename, byte[] content) throws IOException;

    /**
     * Writes a content to a file in a subdirectory
     *
     * @param subdirectory : subdir to write to
     * @param filename : filename to write to
     * @param content : array of bytes to write
     */
    void write(String subdirectory, String filename, byte[] content) throws IOException;

    /**
     * copies the content of a directory to the root subdirectory
     *
     *
     * @param source : directory with the content to copy
     */
    void copy(Path source);

    /**
     * copies the content of a directory to a subdirectory
     *
     * @param subdirectory : subdir to copy to (destination)
     * @param source : directory with the content to copy
     */
    void copy(String subdirectory, Path source);

    Path load(String filename);

    Path load(String subdirectory, String filename);

    Stream<Path> loadAll();

    Resource loadAsResource(String filename);

    Resource loadAsResource(String subdirectory, String filename);

    void delete(String filename);

    void delete(String subdirectory, String filename);

    void deleteAll();

    void createSubdirectory(String subdirectory);

    void deleteSubdirectory(String subdirectory);

}

