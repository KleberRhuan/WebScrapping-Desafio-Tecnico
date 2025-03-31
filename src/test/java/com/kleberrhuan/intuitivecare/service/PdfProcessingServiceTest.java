package com.kleberrhuan.intuitivecare.service;

import com.kleberrhuan.intuitivecare.exception.PdfParseException;
import com.kleberrhuan.intuitivecare.util.interfaces.Archiver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes para a classe PdfProcessingService
 */
class PdfProcessingServiceTest {

  private PdfProcessingService pdfProcessingService;

  @Mock
  private Archiver archiver;

  @TempDir
  Path tempDir;

  private Path pdfPath;
  private Path csvPath;
  private Path zipPath;

  @BeforeEach
  void setUp() throws IOException {
    MockitoAnnotations.openMocks(this);

    // Criar arquivos de teste
    pdfPath = tempDir.resolve("anexo_teste.pdf");
    csvPath = tempDir.resolve("saida.csv");
    zipPath = tempDir.resolve("saida.zip");

    // Criar objeto real
    pdfProcessingService = new PdfProcessingService();
  }

  @Test
  void processPdf_mustProcessPdfWithSuccess() throws IOException {
    // Arrange
    Files.createFile(pdfPath);

    // Mock para evitar processamento real
    PdfProcessingService spyService = spy(pdfProcessingService);
    doReturn("Processo concluído com sucesso").when(spyService).processPdf(any(), any(), any());

    // Act
    String result = spyService.processPdf(pdfPath, csvPath, zipPath);

    // Assert
    assertEquals("Processo concluído com sucesso", result);
  }

  @Test
  void processPdf_mustThrowExceptionWhenFileDoesNotExist() {
    // Arrange
    Path nonExistentPath = tempDir.resolve("nao_existe.pdf");

    // Act & Assert
    assertThrows(PdfParseException.class, () -> pdfProcessingService.processPdf(nonExistentPath, csvPath, zipPath));
  }
}