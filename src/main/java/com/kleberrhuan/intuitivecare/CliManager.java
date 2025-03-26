package com.kleberrhuan.intuitivecare;

import com.kleberrhuan.intuitivecare.exception.ZipException;
import com.kleberrhuan.intuitivecare.model.FileModel;
import com.kleberrhuan.intuitivecare.model.FileType;
import com.kleberrhuan.intuitivecare.model.ScrappingRequest;
import com.kleberrhuan.intuitivecare.service.ScrapperService;
import com.kleberrhuan.intuitivecare.util.ScannerHelper;
import com.kleberrhuan.intuitivecare.util.ZipArchiver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

/**
 * Refactored CLI Manager to handle application operations.
 * It displays a clean menu, retrieves user input using ScannerHelper,
 * and executes the download and ZIP archive operations.
 */
public class CliManager {
    private final ScannerHelper scannerHelper;

    public CliManager(ScannerHelper scannerHelper) {
        this.scannerHelper = scannerHelper;
    }
    
    public void run() {
        boolean exit = false;
        while (!exit) {
            try {
                showMenu();
                int choice = scannerHelper.getInt("Enter your choice: ");
                switch (choice) {
                    case 1:
                        downloadFilesOperation();
                        break;
                    case 2:
                        zipArchiveOperation();
                        break;
                    case 3:
                        exit = true;
                        System.out.println("Exiting application. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (Exception e) {
                System.err.println("An unexpected error occurred: " + e.getMessage());
            }
        }
        scannerHelper.close();
    }
    
    private void showMenu() {
        System.out.println("\n=== Main Menu ===");
        System.out.println("1. Download files");
        System.out.println("2. Create ZIP archive of downloaded files");
        System.out.println("3. Exit");
    }

    private void downloadFilesOperation() {
        System.out.println("\nStarting file download operation...");
        List<FileModel> files = List.of(
                new FileModel("Anexo I.", FileType.PDF),
                new FileModel("Anexo II.", FileType.PDF)
        );

        ScrappingRequest scrappingRequest = new ScrappingRequest(
                "https://www.gov.br/ans/pt-br/acesso-a-informacao/participacao-da-sociedade/atualizacao-do-rol-de-procedimentos",
                files
        );
        ScrapperService scrapperService = new ScrapperService(scrappingRequest);
        String downloadDir = scannerHelper.getString("Enter the download directory (absolute path): ");
        try {
            scrapperService.downloadFiles(downloadDir);
            System.out.println("Files downloaded successfully.");
        } catch (Exception e) {
            System.err.println("Error during download: " + e.getMessage());
        }
    }
    
    private void zipArchiveOperation() {
        System.out.println("\nCreating ZIP archive of files...");
        String filesDirStr = scannerHelper.getString("Enter the directory containing files to zip (absolute path): ");
        String zipPathStr = scannerHelper.getString("Enter the output path for the ZIP archive (absolute path): ");
        String zipName = scannerHelper.getString("Enter the name for the ZIP archive: ");
        Path dir = Paths.get(filesDirStr);
        try (Stream<Path> paths = Files.list(dir)) {
            List<Path> filesToZip = paths.filter(Files::isRegularFile).toList();
            if (filesToZip.isEmpty()) {
                System.out.println("No files found in the specified directory.");
                return;
            }
            ZipArchiver zipArchiver = new ZipArchiver();
            zipArchiver.archiveFiles(filesToZip, Paths.get(zipPathStr), zipName);
            System.out.println("ZIP archive created successfully at " + zipPathStr);
        } catch (IOException e) {
            throw new ZipException("Error creating ZIP archive: " + e.getMessage(), e.getCause());
        }
    }
}