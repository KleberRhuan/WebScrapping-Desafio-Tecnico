package com.kleberrhuan.intuitivecare.util;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for directory operations.
 * This class provides methods to validate and create directories, ensuring that the input path is valid.
 */
public final class DirectoryHelper {
    private DirectoryHelper() {}
    
    /**
     * Creates the specified directory if it does not exist.
     * If the path exists but is not a directory, an IllegalArgumentException is thrown.
     *
     * @param dir the directory path to create.
     * @return the existing or newly created directory path.
     * @throws IOException if an I/O error occurs during creation.
     * @throws IllegalArgumentException if the provided path is null or exists and is not a directory.
     */
    public static Path createDirectoryIfNotExists(@NotNull Path dir) throws IOException {
        boolean exists = Files.exists(dir);
        if (exists && !Files.isDirectory(dir)) {
            throw new IllegalArgumentException("The path " + dir + " exists, but is not a directory.");
        } else if (!exists) {
            Files.createDirectories(dir);
        }
        return dir;
    }

    /**
     * Creates the specified directory from a String if it does not exist.
     *
     * @param directoryPath the directory path as a String.
     * @return the existing or newly created directory path.
     * @throws IOException if an I/O error occurs during creation.
     * @throws IllegalArgumentException if the provided path is null or empty.
     */
    public static Path createDirectoryIfNotExists(String directoryPath) throws IOException {
        if (directoryPath == null || directoryPath.isEmpty()) {
            throw new IllegalArgumentException("The directory path cannot be null or empty.");
        }
        Path dir = Paths.get(directoryPath);
        return createDirectoryIfNotExists(dir);
    }
    
    public static String getZipFileName(@NotBlank String fileName) {
        if (fileName.toLowerCase().endsWith(".zip")) {
            return fileName;
        } else {
            return fileName + ".zip";
        }
    }
}