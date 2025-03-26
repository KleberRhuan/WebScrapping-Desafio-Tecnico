package com.kleberrhuan.intuitivecare.util;

import com.kleberrhuan.intuitivecare.exception.ZipException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A utility class that implements the Archiver interface to archive files into a ZIP file.
 */
public class ZipArchiver implements Archiver {

    /**
     * Archives a list of files into a ZIP file.
     *
     * @param files       List of file paths to be archived.
     * @param destDir The path where the ZIP archive will be created.
     * @throws IOException If an error occurs during reading/writing files.
     */
    @Override
    public void archiveFiles(List<Path> files, 
                             @NotNull Path destDir, 
                             @NotBlank String archiveName) throws IOException {
        DirectoryHelper.createDirectoryIfNotExists(destDir);
        Path archivePath = destDir.resolve(DirectoryHelper.getZipFileName(archiveName));
        try (ZipOutputStream zipOut = createZipOutputStream(archivePath)) {
            for (Path file : files) {
                addFileToZip(file, zipOut);
            }
        } catch (IOException e) {
            throw new ZipException("An error occurred while creating the ZIP archive: " + archivePath, e);
    }}

    /**
     * Creates a ZipOutputStream to write the ZIP archive.
     * Opens a SeekableByteChannel with options to create or truncate the existing file.
     *
     * @param archivePath The path to the archive file.
     * @return A ZipOutputStream wrapping the channel's output stream.
     * @throws IOException If an I/O error occurs while opening the channel.
     */
    private ZipOutputStream createZipOutputStream(Path archivePath) throws IOException {
        SeekableByteChannel outChannel = Files.newByteChannel(
                archivePath,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );
        return new ZipOutputStream(Channels.newOutputStream(outChannel));
    }

    /**
     * Adds a single file to the ZIP archive.
     * If the file does not exist, it logs an error message and skips it.
     *
     * @param file   The path of the file to add.
     * @param zipOut The ZipOutputStream to write the file into.
     * @throws IOException If an error occurs during file I/O.
     */
    private void addFileToZip(Path file, ZipOutputStream zipOut) throws IOException {
        if (Files.notExists(file)) {
            System.err.println("Skipping non-existent file: " + file);
            return;
        }
        ZipEntry zipEntry = createZipEntry(file);
        zipOut.putNextEntry(zipEntry);
        copyFileToZip(file, zipOut);
        zipOut.closeEntry();
        System.out.println("Added to ZIP: " + file);
    }

    /**
     * Creates a ZipEntry for a given file using its file name.
     *
     * @param file The file for which to create a ZIP entry.
     * @return A new ZipEntry with the file name.
     */
    private ZipEntry createZipEntry(Path file) {
        String fileNameInsideZip = file.getFileName().toString();
        return new ZipEntry(fileNameInsideZip);
    }

    /**
     * Copies the content of the file into the ZIP output stream.
     * It uses a BufferedInputStream for efficient reading.
     *
     * @param file   The file to copy into the archive.
     * @param zipOut The ZipOutputStream to write the file's data.
     * @throws IOException If an error occurs during file reading or writing.
     */
    private void copyFileToZip(Path file, ZipOutputStream zipOut) throws IOException {
        try (
                SeekableByteChannel inChannel = Files.newByteChannel(file, StandardOpenOption.READ);
                BufferedInputStream in = new BufferedInputStream(Channels.newInputStream(inChannel))
        ) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                zipOut.write(buffer, 0, bytesRead);
            }
        }
    }
}