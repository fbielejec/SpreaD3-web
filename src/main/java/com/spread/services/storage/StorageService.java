package com.spread.services.storage;

import java.nio.file.Path;
import java.util.stream.Stream;

import com.spread.exceptions.SpreadException;
import com.spread.loggers.AbstractLogger;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    void init(Path rootLocation, AbstractLogger logger);

    void createRootDir() throws SpreadException;

    Boolean isInitialized();

    /**
     * Predicate whether a given file exist in a root directory
     *
     * @param  file to check
     * @return boolean value.
     */
    boolean exists(MultipartFile file) throws SpreadException;

    /**
     * Predicate whether a given file exist in a subdirectory
     *
     * @param  subdirectory : location to check
     * @param  file to check for
     * @return boolean value.
     */
    boolean exists(String subdirectory, MultipartFile file) throws SpreadException;

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
    void store(MultipartFile file) throws SpreadException;

    /**
     * stores a file in the subdirectory
     *
     * @param subdirectory : subdir to store the file in
     * @param file : file to store
     */
    void store(String subdirectory, MultipartFile file) throws SpreadException;

    /**
     * Writes a content to a file in the root directory
     *
     * @param filename : filename to write to
     * @param content : array of bytes to write
     */
    void write(String filename, byte[] content) throws SpreadException;

    /**
     * Writes a content to a file in a subdirectory
     *
     * @param subdirectory : subdir to write to
     * @param filename : filename to write to
     * @param content : array of bytes to write
     */
    void write(String subdirectory, String filename, byte[] content) throws SpreadException;

    /**
     * copies the content of a directory to the root subdirectory
     *
     *
     * @param source : directory with the content to copy
     */
    void copy(Path source) throws SpreadException;

    /**
     * copies the content of a directory to a subdirectory
     *
     * @param subdirectory : subdir to copy to (destination)
     * @param source : directory with the content to copy
     */
    void copy(String subdirectory, Path source) throws SpreadException;

    Path load(String filename);

    Path load(String subdirectory, String filename);

    Stream<Path> loadAll() throws SpreadException;

    Resource loadAsResource(String filename) throws SpreadException;

    Resource loadAsResource(String subdirectory, String filename) throws SpreadException;

    void delete(String filename);

    void delete(String subdirectory, String filename);

    void deleteAll();

    void createSubdirectory(String subdirectory) throws SpreadException;

    void deleteSubdirectory(String subdirectory);

}
