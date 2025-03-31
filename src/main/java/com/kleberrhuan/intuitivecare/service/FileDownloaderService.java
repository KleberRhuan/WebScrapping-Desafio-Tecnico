package com.kleberrhuan.intuitivecare.service;

import com.kleberrhuan.intuitivecare.model.FilelinkModel;
import com.kleberrhuan.intuitivecare.util.HttpDownloader;
import com.kleberrhuan.intuitivecare.util.ZipManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Service responsible for downloading file links from the specified base URL.
 */
@AllArgsConstructor
@Getter
public class FileDownloaderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileDownloaderService.class);

    private final String baseUrl;
    private final HttpDownloader httpDownloader;

    /**
     * Downloads files from the latest N year folders.
     *
     * @param numberOfYears number of recent years to download from
     * @throws IOException if there is an error accessing or creating file directories
     */
    public void downloadLatestYears(int numberOfYears) throws IOException {
        List<String> allFolders = retrieveYearFolders();
        allFolders.sort(Comparator.reverseOrder());

        int limit = Math.min(numberOfYears, allFolders.size());
        List<String> latestFolders = allFolders.subList(0, limit);
        for (String folder : latestFolders) {
            String folderUrl = baseUrl + folder + "/";
            String localDir = "downloads/" + folder;
            downloadAllZipsInFolder(folderUrl, localDir);
        }
    }

    /**
     * Retrieves the year folders from the configured base URL.
     *
     * @return a list of year-folder names
     * @throws IOException if there is an error connecting or parsing the remote page
     */
    public List<String> retrieveYearFolders() throws IOException {
        Document doc = Jsoup.connect(baseUrl).get();
        Elements links = doc.select("a[href]");
        List<String> folders = new ArrayList<>();

        for (Element link : links) {
            String href = link.attr("href");
            if (href.matches("\\d+/")) {
                folders.add(href.replace("/", ""));
            }
        }

        return folders;
    }

    /**
     * Finds all zip files in a given folder URL and downloads them in parallel.
     *
     * @param folderUrl folder URL to scan
     * @param localDir  local directory to save downloads
     * @throws IOException if there is an error creating directories or connecting to the folder URL
     */
    private void downloadAllZipsInFolder(String folderUrl, String localDir) throws IOException {
        LOGGER.info("Accessing folder: {}", folderUrl);
        Files.createDirectories(Paths.get(localDir));

        Document doc = Jsoup.connect(folderUrl).get();
        Elements links = doc.select("a[href]");
        
        links.parallelStream().forEach(link -> {
            String fileHref = link.attr("href");
            if (fileHref.endsWith(".zip")) {
                String fileUrl = folderUrl + fileHref;
                LOGGER.info("Downloading file: {}", fileUrl);
                try {
                    FilelinkModel fileLink = new FilelinkModel(fileHref, fileUrl);
                    httpDownloader.downloadFile(fileLink, localDir);
                    ZipManager.extractZip(Paths.get(localDir + "/" + fileHref), Paths.get(localDir));
                } catch (IOException e) {
                    LOGGER.error("Failed to download file: {}", fileUrl, e);
                }
            }
        });
    }
    
    public static void main(String[] args) {
        FileDownloaderService service = new FileDownloaderService("https://dadosabertos.ans.gov.br/FTP/PDA/demonstracoes_contabeis/", 
                new HttpDownloader(4));
        try {
            service.downloadLatestYears(2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    } 

}