package com.kleberrhuan.intuitivecare.util;

import com.kleberrhuan.intuitivecare.exception.DirectoryCreationException;
import com.kleberrhuan.intuitivecare.exception.FileDownloadException;
import com.kleberrhuan.intuitivecare.model.FilelinkModel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.Executors.newFixedThreadPool;

public  class HttpDownloader implements Downloader {
    private final ExecutorService executor;
    private static final Map<String, ReentrantLock> fileLocks = new ConcurrentHashMap<>();

    public HttpDownloader(@NotNull @Min(1) int poolSize) {
        executor = newFixedThreadPool(poolSize);
    }
    
    @Override
    public void downloadFile(FilelinkModel file, String destDir) throws IOException {
        URI uri = URI.create(file.url());
        URL url = uri.toURL();
        String fileName = getFileName(uri);
        
        Path outputDir = DirectoryHelper.createDirectoryIfNotExists(destDir);
        Path outputPath = outputDir.resolve(fileName);
        ReentrantLock lock = getLockForPath(outputPath);
        lock.lock();
        try (BufferedInputStream in = new BufferedInputStream(url.openStream())) {
            Files.copy(in, outputPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileDownloadException("An error occurred while downloading the file: " + file.name()
                    , e);
        } finally {
            lock.unlock();
        }
    }

    private static ReentrantLock getLockForPath(Path path) {
        return fileLocks.computeIfAbsent(path.toAbsolutePath().toString(),
                p -> new ReentrantLock());
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
