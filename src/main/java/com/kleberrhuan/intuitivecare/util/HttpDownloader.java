package com.kleberrhuan.intuitivecare.util;

import ch.qos.logback.classic.Logger;
import com.kleberrhuan.intuitivecare.exception.FileDownloadException;
import com.kleberrhuan.intuitivecare.model.FilelinkModel;
import com.kleberrhuan.intuitivecare.util.interfaces.Downloader;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.slf4j.LoggerFactory;

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

public class HttpDownloader implements Downloader {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(HttpDownloader.class);
    private final ExecutorService executor;
    private static final Map<String, ReentrantLock> fileLocks = new ConcurrentHashMap<>();

    public HttpDownloader(@NotNull @Min(1) int poolSize) {
        executor = newFixedThreadPool(poolSize);
    }

    @Override
    public void downloadFile(FilelinkModel file, Path outputDir) throws IOException {
        URI uri = URI.create(file.url());
        URL url = uri.toURL();

        Path outputPath = outputDir.resolve(file.name());
        ReentrantLock lock = getLockForPath(outputPath);
        lock.lock();

        try (BufferedInputStream in = new BufferedInputStream(url.openStream())) {
            Files.copy(in, outputPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileDownloadException("Ocorreu um erro ao baixar o arquivo: " + file.name(), e);
        } finally {
            lock.unlock();
        }
    }

    private static ReentrantLock getLockForPath(Path path) {
        return fileLocks.computeIfAbsent(path.toAbsolutePath().toString(),
                p -> new ReentrantLock());
    }

    public CompletableFuture<Void> downloadFileAsync(FilelinkModel file, Path outputDir) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (file.url().isBlank()) {
                    LOGGER.warn("URL do arquivo está em branco para o arquivo: {}", file.name());
                    return;
                }
                this.downloadFile(file, outputDir);
            } catch (IOException e) {
                throw new FileDownloadException("Ocorreu um erro ao baixar o arquivo: " + file.name(), e);
            }
        }, executor);
    }

    public void downloadFiles(List<FilelinkModel> files, Path outputDir) {
        CompletableFuture<?>[] futures = new CompletableFuture[files.size()];
        for (int i = 0; i < files.size(); i++) {
            futures[i] = downloadFileAsync(files.get(i), outputDir);
        }
        CompletableFuture.allOf(futures).join();
    }

    public void shutdown() {
        executor.shutdown();
    }
}
