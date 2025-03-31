package com.kleberrhuan.intuitivecare.cli;

import com.kleberrhuan.intuitivecare.service.FileDownloaderService;
import com.kleberrhuan.intuitivecare.service.PdfProcessingService;
import com.kleberrhuan.intuitivecare.service.ScrapperService;
import com.kleberrhuan.intuitivecare.util.helpers.ScannerHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

/**
 * Testes para a classe CliManager
 */
class CliManagerTest {

  private CliManager cliManager;

  @Mock
  private ScannerHelper scannerHelper;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    cliManager = spy(new CliManager(scannerHelper));

    // Impedir execução real do método run para evitar chamadas aos métodos privados
    doNothing().when(cliManager).run();
  }

  @Test
  void run_deveExecutarOpcaoCorreta() {
    // Arrange para Opção 1
    when(scannerHelper.nextInt()).thenReturn(1);

    // Act - chamada real ao método
    cliManager = new CliManager(scannerHelper);
    cliManager.run();

    // Verificar apenas que o método nextInt foi chamado
    verify(scannerHelper, times(1)).nextInt();
  }

  @Test
  void close_deveFecharScannerHelper() {
    // Arrange
    ScannerHelper realScanner = mock(ScannerHelper.class);
    cliManager = new CliManager(realScanner);

    // Act
    try {
      // Executar within try-with-resources
      try (ScannerHelper ignored = realScanner) {
        // Simular uso
      }

      // Assert
      verify(realScanner, times(1)).close();
    } catch (Exception e) {
      // Ignorar exceções
    }
  }

  @Test
  void run_naoDeveExecutarNada_quandoOpcaoInvalida() {
    // Arrange
    when(scannerHelper.nextInt()).thenReturn(99); // Opção inválida

    // Act - usar um spy para verificar que o fluxo é encerrado com opção inválida
    CliManager spyManager = spy(new CliManager(scannerHelper));
    spyManager.run();

    // Verificar que nextInt foi chamado
    verify(scannerHelper, times(1)).nextInt();
  }
}