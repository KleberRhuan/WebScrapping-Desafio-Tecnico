package com.kleberrhuan.intuitivecare.util;

import com.kleberrhuan.intuitivecare.exception.ZipException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para a classe ZipManager
 */
class ZipManagerTest {

  private ZipManager zipManager;

  @TempDir
  Path tempDir;

  private Path file1;
  private Path file2;

  @BeforeEach
  void setUp() throws IOException {
    zipManager = new ZipManager();

    // Criar arquivos de teste
    file1 = tempDir.resolve("teste1.txt");
    file2 = tempDir.resolve("teste2.txt");

    Files.writeString(file1, "Conteúdo de teste 1");
    Files.writeString(file2, "Conteúdo de teste 2");
  }

  @Test
  void archiveFiles_deveCompactarArquivosComSucesso() throws IOException {
    // Arrange
    List<Path> files = Arrays.asList(file1, file2);
    Path outputDir = tempDir.resolve("output");
    Files.createDirectories(outputDir);
    String zipName = "arquivos_teste";

    // Act
    zipManager.archiveFiles(files, outputDir, zipName);

    // Assert
    Path zipPath = outputDir.resolve(zipName + ".zip");
    assertTrue(Files.exists(zipPath), "Arquivo ZIP não foi criado");
    assertTrue(Files.size(zipPath) > 0, "Arquivo ZIP está vazio");
  }

  @Test
  void extractZip_deveExtrairArquivosComSucesso() throws IOException {
    // Arrange
    List<Path> files = Arrays.asList(file1, file2);
    Path outputDir = tempDir.resolve("output");
    Files.createDirectories(outputDir);
    String zipName = "arquivos_teste";

    zipManager.archiveFiles(files, outputDir, zipName);
    Path zipPath = outputDir.resolve(zipName + ".zip");

    Path extractDir = tempDir.resolve("extract");
    Files.createDirectories(extractDir);

    // Act
    ZipManager.extractZip(zipPath, extractDir);

    // Assert
    assertTrue(Files.exists(extractDir.resolve("teste1.txt")), "Arquivo 1 não foi extraído");
    assertTrue(Files.exists(extractDir.resolve("teste2.txt")), "Arquivo 2 não foi extraído");

    String content1 = Files.readString(extractDir.resolve("teste1.txt"));
    assertEquals("Conteúdo de teste 1", content1, "Conteúdo do arquivo 1 não corresponde");
  }

  @Test
  void archiveFiles_deveLidarComArquivosInexistentes() {
    // Arrange
    Path nonExistentFile = tempDir.resolve("nao_existe.txt");
    List<Path> files = Arrays.asList(nonExistentFile);

    // Act & Assert
    assertDoesNotThrow(() -> zipManager.archiveFiles(files, tempDir, "test"),
        "Deve ignorar arquivos que não existem");
  }
}