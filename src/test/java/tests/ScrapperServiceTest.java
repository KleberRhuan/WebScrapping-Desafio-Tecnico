package tests;

import com.kleberrhuan.intuitivecare.exception.WebsiteConnectionException;
import com.kleberrhuan.intuitivecare.model.FileModel;
import com.kleberrhuan.intuitivecare.model.FileType;
import com.kleberrhuan.intuitivecare.model.ScrappingRequest;
import com.kleberrhuan.intuitivecare.service.ScrapperService;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

class ScrapperServiceTest {
    @TempDir
    Path tempDir;

    /**
     * Tests the downloadFiles method when a valid HTML page is returned.
     * A dummy HTML page is simulated (using Mockito) with an anchor whose href points
     * to a temporary file. After downloadFiles is executed, the destination directory
     * should contain the expected file with matching content.
     */
    @Test
    void testDownloadFilesSuccess() throws Exception {
        Path dummySourceFile = Files.createTempFile(tempDir, "dummy", ".pdf");
        String dummyContent = "Dummy file content for testing";
        Files.write(dummySourceFile, dummyContent.getBytes(StandardCharsets.UTF_8));
        
        String fileUrl = dummySourceFile.toUri().toString();
        String dummyHtml = "<html><body><a href='" + fileUrl + "'>dummy</a></body></html>";
        Document dummyDocument = Jsoup.parse(dummyHtml, "", Parser.htmlParser());

        ScrappingRequest request = ScrappingRequest.builder()
                .url("http://example.com") // the actual URL is irrelevant due to our mocking
                .files(List.of(new FileModel("dummy", FileType.PDF)))
                .build();
        
        try (MockedStatic<Jsoup> jsoupMock = Mockito.mockStatic(Jsoup.class)) {
            Connection connectionMock = Mockito.mock(Connection.class);
            jsoupMock.when(() -> Jsoup.connect(anyString())).thenReturn(connectionMock);
            Mockito.when(connectionMock.get()).thenReturn(dummyDocument);
            
            Path destDir = Files.createDirectory(tempDir.resolve("dest"));

            ScrapperService service = new ScrapperService(request);
            service.downloadFiles(destDir.toString());
            
            Path downloadedFile = destDir.resolve(dummySourceFile.getFileName().toString());
            assertTrue(Files.exists(downloadedFile), "Downloaded file should exist.");
            String downloadedContent = Files.readString(downloadedFile, StandardCharsets.UTF_8);
            assertEquals(dummyContent, downloadedContent, "Downloaded file content should match the dummy file content.");
        }
    }

    /**
     * Tests the downloadFiles method when an IOException occurs during the connection.
     * Mockito is used to simulate an IO failure on the Jsoup.connect(...).get() call.
     * The method should then throw a WebsiteConnectionException.
     */
    @Test
    void testDownloadFilesWebsiteConnectionException() {
        ScrappingRequest request = ScrappingRequest.builder()
                .url("http://invalid-url.com")
                .files(List.of(new FileModel("nonexistent", FileType.PDF)))
                .build();
        
        try (MockedStatic<Jsoup> jsoupMock = Mockito.mockStatic(Jsoup.class)) {
            Connection connectionMock = Mockito.mock(Connection.class);
            jsoupMock.when(() -> Jsoup.connect(anyString())).thenReturn(connectionMock);
            try {
                Mockito.when(connectionMock.get()).thenThrow(new java.io.IOException("Simulated connection error"));
            } catch (Exception e) {
                // This block is not expected to execute.
            }

            ScrapperService service = new ScrapperService(request);
            WebsiteConnectionException exception = assertThrows(WebsiteConnectionException.class, () ->
                    service.downloadFiles(tempDir.resolve("dest").toString()));
            assertTrue(exception.getMessage().contains(request.getUrl()),
                    "Exception message should contain the request URL.");
        }
    }
}
