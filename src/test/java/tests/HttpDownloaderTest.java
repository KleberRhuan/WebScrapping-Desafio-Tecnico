package tests; 

import com.kleberrhuan.intuitivecare.exception.FileDownloadException;
import com.kleberrhuan.intuitivecare.model.FilelinkModel;
import com.kleberrhuan.intuitivecare.util.HttpDownloader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
 
class HttpDownloaderTest {

    @TempDir
    Path tempDir;

    /**
     * Tests synchronous download of a file.
     * Creates a temporary source file and downloads it using HttpDownloader.
     * Verifies that the downloaded file exists and its content matches the source.
     */
    @Test
    void testDownloadFileSuccess() throws IOException {
        Path sourceDir = Files.createDirectory(tempDir.resolve("source"));
        String fileContent = "Sample content for testing";
        Path sourceFile = Files.createFile(sourceDir.resolve("testFile.txt"));
        Files.write(sourceFile, fileContent.getBytes());
 
        FilelinkModel filelinkModel = new FilelinkModel("testFile.txt", sourceFile.toUri().toString());
        
        Path destDirPath = tempDir.resolve("dest");
        String destDir = destDirPath.toString();

        HttpDownloader downloader = new HttpDownloader(2);
        downloader.downloadFile(filelinkModel, destDir);
        
        Path downloadedFile = destDirPath.resolve("testFile.txt");
        assertTrue(Files.exists(downloadedFile), "Downloaded file should exist.");
        String downloadedContent = Files.readString(downloadedFile);
        assertEquals(fileContent, downloadedContent, "Downloaded file content should match source content.");

        downloader.shutdown();
    }

    /**
     * Tests asynchronous download of a file.
     * Uses downloadFileAsync to download a file and verifies its successful download.
     */
    @Test
    void testDownloadFileAsyncSuccess() throws IOException, InterruptedException, ExecutionException {

        Path sourceDir = Files.createDirectory(tempDir.resolve("sourceAsync"));
        String fileContent = "Async test content";
        Path sourceFile = Files.createFile(sourceDir.resolve("asyncFile.txt"));
        Files.write(sourceFile, fileContent.getBytes());

        FilelinkModel filelinkModel = new FilelinkModel("asyncFile.txt", sourceFile.toUri().toString());
        Path destDirPath = tempDir.resolve("destAsync");
        String destDir = destDirPath.toString();

        HttpDownloader downloader = new HttpDownloader(2);
        downloader.downloadFileAsync(filelinkModel, destDir).get();
        
        Path downloadedFile = destDirPath.resolve("asyncFile.txt");
        assertTrue(Files.exists(downloadedFile), "Downloaded file should exist.");
        String downloadedContent = Files.readString(downloadedFile);
        assertEquals(fileContent, downloadedContent, "Downloaded file content should match source content.");

        downloader.shutdown();
    }

    /**
     * Tests downloading multiple files concurrently.
     * Creates multiple source files, downloads them using downloadFiles, and verifies each file.
     */
    @Test
    void testDownloadFilesMultipleSuccess() throws IOException {

        Path sourceDir = Files.createDirectory(tempDir.resolve("sourceMulti"));
        String content1 = "Content for file 1";
        String content2 = "Content for file 2";
        Path sourceFile1 = Files.createFile(sourceDir.resolve("file1.txt"));
        Path sourceFile2 = Files.createFile(sourceDir.resolve("file2.txt"));
        Files.write(sourceFile1, content1.getBytes());
        Files.write(sourceFile2, content2.getBytes());

        FilelinkModel filelinkModel = new FilelinkModel("file1.txt", sourceFile1.toUri().toString());
        FilelinkModel filelinkModel1 = new FilelinkModel("file2.txt", sourceFile2.toUri().toString());
        List<FilelinkModel> filelinks = Arrays.asList(filelinkModel, filelinkModel1);
        
        Path destDirPath = tempDir.resolve("destMulti");
        String destDir = destDirPath.toString();

        HttpDownloader downloader = new HttpDownloader(2);
        downloader.downloadFiles(filelinks, destDir);
        
        Path downloadedFile1 = destDirPath.resolve("file1.txt");
        Path downloadedFile2 = destDirPath.resolve("file2.txt");
        assertTrue(Files.exists(downloadedFile1), "File1 should be downloaded.");
        assertTrue(Files.exists(downloadedFile2), "File2 should be downloaded.");
        assertEquals(content1, Files.readString(downloadedFile1), "File1 content should match.");
        assertEquals(content2, Files.readString(downloadedFile2), "File2 content should match.");

        downloader.shutdown();
    }

    /**
     * Tests downloadFile method with an invalid URL.
     * Expects a FileDownloadException to be thrown when the URL is invalid.
     */
    @Test
    void testDownloadFileInvalidUrl() {
         FilelinkModel filelinkModel = new FilelinkModel("invalid.txt", "file:///nonexistent/path/invalid.txt");
        Path destDirPath = tempDir.resolve("destInvalid");
        String destDir = destDirPath.toString();

        HttpDownloader downloader = new HttpDownloader(2);
        FileDownloadException exception = assertThrows(FileDownloadException.class, () -> {
            downloader.downloadFile(filelinkModel, destDir);
        });
        assertTrue(exception.getMessage().contains("invalid.txt"), "Exception message should contain file name.");

        downloader.shutdown();
    }
}