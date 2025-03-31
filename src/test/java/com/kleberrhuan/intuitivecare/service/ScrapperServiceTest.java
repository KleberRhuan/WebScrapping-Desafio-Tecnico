package com.kleberrhuan.intuitivecare.service;

import com.kleberrhuan.intuitivecare.exception.WebsiteConnectionException;
import com.kleberrhuan.intuitivecare.model.FileModel;
import com.kleberrhuan.intuitivecare.model.FileType;
import com.kleberrhuan.intuitivecare.model.ScrappingRequest;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes para a classe ScrapperService
 */
class ScrapperServiceTest {

  private ScrapperService scrapperService;

  @Mock
  private ScrappingRequest scrappingRequest;

  @Mock
  private Document document;

  @Mock
  private Connection connection;

  @Mock
  private Element element1;

  @Mock
  private Element element2;

  @Mock
  private Elements elements;

  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    // Inicialize o mock elements
    element1 = mock(Element.class);
    element2 = mock(Element.class);
    elements = mock(Elements.class);

    when(element1.attr("href")).thenReturn("anexo1.pdf");
    when(element1.text()).thenReturn("Anexo I");
    when(element2.attr("href")).thenReturn("anexo2.pdf");
    when(element2.text()).thenReturn("Anexo II");

    when(elements.iterator()).thenReturn(Arrays.asList(element1, element2).iterator());
    when(elements.stream()).thenReturn(Arrays.asList(element1, element2).stream());

    // Configurar mocks
    List<FileModel> files = Arrays.asList(
        new FileModel("Anexo I", FileType.PDF),
        new FileModel("Anexo II", FileType.PDF));

    when(scrappingRequest.getUrl()).thenReturn("http://teste.com");
    when(scrappingRequest.getFiles()).thenReturn(files);
    when(scrappingRequest.getFilesTypeExtensions()).thenReturn(List.of(".pdf"));

    scrapperService = new ScrapperService(scrappingRequest);
  }

  @Test
  void downloadFiles_mustThrowExceptionWhenConnectionFails() throws IOException {
    // Arrange
    try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
      jsoupMock.when(() -> Jsoup.connect(anyString())).thenReturn(connection);
      when(connection.get()).thenThrow(new IOException("Erro de conexão"));

      // Act & Assert
      assertThrows(WebsiteConnectionException.class, () -> scrapperService.downloadFiles(tempDir));
    }
  }

  @Test
  void hasMatchingText_mustReturnTrueWhenTextMatches() throws ReflectiveOperationException {
    // Arrange
    Element element = mock(Element.class);
    when(element.text()).thenReturn("Anexo I - Documento");

    // Act - usando reflexão para acessar método privado
    java.lang.reflect.Method method = ScrapperService.class.getDeclaredMethod(
        "hasMatchingText", Element.class, String.class);
    method.setAccessible(true);
    boolean result = (boolean) method.invoke(scrapperService, element, "Anexo I");

    // Assert
    assertTrue(result);
  }
}