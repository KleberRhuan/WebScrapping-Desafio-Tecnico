package com.kleberrhuan.intuitivecare.util;

import com.kleberrhuan.intuitivecare.exception.FileDownloadException;
import com.kleberrhuan.intuitivecare.model.FilelinkModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes para a classe HttpDownloader
 */
class HttpDownloaderTest {

  private HttpDownloader httpDownloader;

  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    httpDownloader = new HttpDownloader(2);
  }

  @AfterEach
  void tearDown() {
    httpDownloader.shutdown();
  }

  @Test
  void downloadFileAsync_deveRetornarCompletableFutureCompleto()
      throws ExecutionException, InterruptedException, IOException {
    // Arrange
    FilelinkModel fileMock = mock(FilelinkModel.class);
    when(fileMock.url()).thenReturn("http://exemplo.com/arquivo.txt");
    when(fileMock.name()).thenReturn("arquivo.txt");

    HttpDownloader spyDownloader = spy(httpDownloader);
    doNothing().when(spyDownloader).downloadFile(any(), any());

    // Act
    CompletableFuture<Void> future = spyDownloader.downloadFileAsync(fileMock, tempDir);

    // Assert
    assertNotNull(future);
    future.get(); // Não deve lançar exceção
    verify(spyDownloader, times(1)).downloadFile(eq(fileMock), eq(tempDir));
  }

  @Test
  void downloadFileAsync_deveLidarComUrlVazia() throws IOException {
    // Arrange
    FilelinkModel fileMock = mock(FilelinkModel.class);
    when(fileMock.url()).thenReturn("");
    when(fileMock.name()).thenReturn("arquivo.txt");

    // Act
    CompletableFuture<Void> future = httpDownloader.downloadFileAsync(fileMock, tempDir);

    // Assert
    assertNotNull(future);
    assertDoesNotThrow(() -> future.get());
  }

  @Test
  void downloadFiles_deveProcessarListaDeArquivos() {
    // Arrange
    HttpDownloader spyDownloader = spy(httpDownloader);

    FilelinkModel file1 = mock(FilelinkModel.class);
    when(file1.url()).thenReturn("http://exemplo.com/arquivo1.txt");
    when(file1.name()).thenReturn("arquivo1.txt");

    FilelinkModel file2 = mock(FilelinkModel.class);
    when(file2.url()).thenReturn("http://exemplo.com/arquivo2.txt");
    when(file2.name()).thenReturn("arquivo2.txt");

    List<FilelinkModel> files = Arrays.asList(file1, file2);

    doReturn(CompletableFuture.completedFuture(null))
        .when(spyDownloader).downloadFileAsync(any(), any());

    // Act
    assertDoesNotThrow(() -> spyDownloader.downloadFiles(files, tempDir));

    // Assert
    verify(spyDownloader, times(1)).downloadFileAsync(eq(file1), eq(tempDir));
    verify(spyDownloader, times(1)).downloadFileAsync(eq(file2), eq(tempDir));
  }
}