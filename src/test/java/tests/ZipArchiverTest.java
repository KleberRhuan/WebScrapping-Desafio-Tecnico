package tests;

import com.kleberrhuan.intuitivecare.util.ZipArchiver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

class ZipArchiverTest {

    @TempDir
    Path tempDir;

    /**
     * Tests archiving an empty list of files.
     * Ensures that a ZIP file is created and contains no entries.
     */
    @Test
    void testArchiveEmptyFilesList() throws IOException {
        ZipArchiver archiver = new ZipArchiver();
        List<Path> files = Collections.emptyList();
        String archiveName = "emptyArchive";

        archiver.archiveFiles(files, tempDir, archiveName);

        Path zipFilePath = tempDir.resolve(archiveName + ".zip");
        assertTrue(Files.exists(zipFilePath), "The ZIP file should have been created.");

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFilePath))) {
            ZipEntry entry = zis.getNextEntry();
            assertNull(entry, "The ZIP file should not contain any entries.");
        }
    }

    /**
     * Tests archiving a single file.
     * Verifies that the ZIP file contains the correct file and content.
     */
    @Test
    void testArchiveSingleFile() throws IOException {
        ZipArchiver archiver = new ZipArchiver();

        Path file = Files.createTempFile(tempDir, "testFile", ".txt");
        String content = "Olá, ZIP!";
        Files.write(file, content.getBytes());

        List<Path> files = Arrays.asList(file);
        String archiveName = "singleFileArchive";

        archiver.archiveFiles(files, tempDir, archiveName);

        Path zipFilePath = tempDir.resolve(archiveName + ".zip");
        assertTrue(Files.exists(zipFilePath), "The ZIP file should have been created.");

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFilePath))) {
            ZipEntry entry = zis.getNextEntry();
            assertNotNull(entry, "The ZIP file should contain one entry.");
            assertEquals(file.getFileName().toString(), entry.getName(), "The entry name should match the original file name.");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = zis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            String zipContent = baos.toString();
            assertEquals(content, zipContent, "The content of the file in the ZIP should match the original content.");

            assertNull(zis.getNextEntry(), "There should be no additional entries in the ZIP file.");
        }
    }

    /**
     * Tests archiving a list that includes a non-existent file.
     * Ensures that only valid files are included in the ZIP archive.
     */
    @Test
    void testArchiveWithNonExistentFile() throws IOException {
        ZipArchiver archiver = new ZipArchiver();

        Path validFile = Files.createTempFile(tempDir, "validFile", ".txt");
        String validContent = "Valid file content";
        Files.write(validFile, validContent.getBytes());

        Path nonExistentFile = tempDir.resolve("nonexistent.txt");

        List<Path> files = Arrays.asList(validFile, nonExistentFile);
        String archiveName = "archiveWithMissingFile";

        archiver.archiveFiles(files, tempDir, archiveName);

        Path zipFilePath = tempDir.resolve(archiveName + ".zip");
        assertTrue(Files.exists(zipFilePath), "The ZIP file should have been created.");

        int entryCount = 0;
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entryCount++;
                assertEquals(validFile.getFileName().toString(), entry.getName(), "The only entry should be the valid file.");
            }
        }
        assertEquals(1, entryCount, "The ZIP file should contain exactly one entry.");
    }
}