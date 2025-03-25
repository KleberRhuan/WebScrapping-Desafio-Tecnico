package com.kleberrhuan.intuitivecare.util;

import com.kleberrhuan.intuitivecare.exception.DirectoryCreationException;
import com.kleberrhuan.intuitivecare.exception.FileDownloadException;
import com.kleberrhuan.intuitivecare.model.FilelinkModel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class HttpDownloader implements DownloaderInterface {
    private static final Path DEFAULT_OUTPUT_DIR = Path.of("output");
    private ExecutorService executor;
    
    public HttpDownloader(@NotNull @Min(1) int poolSize) {
        executor = newFixedThreadPool(poolSize);
    }
    
    @Override
    public void downloadFile(FilelinkModel file, String destDir) throws IOException {
        URI uri = URI.create(file.url());
        URL url = uri.toURL();

        String fileName = getFileName(uri);
        
        Path outputDir = getOutputDir(destDir);
        Path outputPath = outputDir.resolve(fileName);

        try (BufferedInputStream in = new BufferedInputStream(url.openStream())) {
            Files.copy(in, outputPath, StandardCopyOption.REPLACE_EXISTING);
        } 
    }

    /**
     * Extracts the file name from the URI's path. 
     * If there's no '/', the entire path is taken as the filename.
     */
    private String getFileName(URI uri) {
        String path = uri.getPath();
        int lastSlashIndex = path.lastIndexOf('/');
        return (lastSlashIndex == -1) ? path : path.substring(lastSlashIndex + 1);
    }

    /**
     * Returns a Path for the output directory. If 'destDir' is null or empty,
     * it returns/creates the default directory. Otherwise, it creates the specified directory
     * if it doesn't already exist.
     */
    private Path getOutputDir(String destDir) {
        if (destDir == null || destDir.isEmpty()) {
            return createDirectoryIfNeeded(DEFAULT_OUTPUT_DIR);
        }
        
        Path userDir = Path.of(destDir);
        return createDirectoryIfNeeded(userDir);
    }
    
    private Path createDirectoryIfNeeded(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new DirectoryCreationException(
                    "An error occurred while creating the output directory: " + dir, e);
        }
        return dir;
    }
    
    public CompletableFuture<Void> downloadFileAsync(FilelinkModel file, String destDir) {
        return CompletableFuture.runAsync(() -> {
            try {
                this.downloadFile(file, destDir);
            } catch (IOException e) {
                throw new FileDownloadException("An error occurred while downloading the file: " + file.name(), e);
            }
        }, executor);
    }
    
    public void downloadFiles(List<FilelinkModel> files, String destDir)  {
        CompletableFuture<?>[] futures = new CompletableFuture[files.size()];
        for (int i = 0; i < files.size(); i++) {
            futures[i] = downloadFileAsync(files.get(i), destDir);
        }

        CompletableFuture.allOf(futures).join();
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}
