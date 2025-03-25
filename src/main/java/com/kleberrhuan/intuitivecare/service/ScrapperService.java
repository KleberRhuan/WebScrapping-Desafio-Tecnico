package com.kleberrhuan.intuitivecare.service;

import com.kleberrhuan.intuitivecare.exception.WebsiteConnectionException;
import com.kleberrhuan.intuitivecare.model.FileType;
import com.kleberrhuan.intuitivecare.model.FilelinkModel;
import com.kleberrhuan.intuitivecare.model.ScrappingRequest;
import com.kleberrhuan.intuitivecare.util.HttpDownloader;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ScrapperService {
    private final ScrappingRequest scrappingRequest;
    private static final HttpDownloader downloader;
    
    static {
        downloader = new HttpDownloader(10);
    }

    /**
     * Connect to the given URL using Jsoup and extract elements that contain one of the file extensions
     * specified in the ScrappingRequest.
     */
    private Elements parsePageForElementsLinks(String url) throws IOException {
        Document document = Jsoup.connect(url).get();
        List<String> fileExtensions = scrappingRequest.getFilesTypeExtensions();
        String cssQuery = fileExtensions.stream()
                .map(ext -> String.format("a[href*='%s']", ext))
                .collect(Collectors.joining(", "));
        return document.select(cssQuery);
    }

    /**
     * Go through each file definition (from the ScrappingRequest) and try to match
     * an element whose text includes the file's name and has the correct file-type extension.
     */
    private List<FilelinkModel> getLinksFromElements(@NotNull Elements elements) {
        return scrappingRequest.getFiles().stream()
                .map(fileModel -> new FilelinkModel(
                        fileModel.getFullName(),
                        elements.stream()
                                .filter(e -> hasMatchingText(e, fileModel.name()))
                                .filter(e -> hasValidExtension(e, fileModel.fileType()))
                                .map(e -> e.attr("abs:href"))
                                .findFirst()
                                .orElse(null)
                ))
                .toList();
    }
    
    public void downloadFiles(String destDir) {
        try {
            Elements elements = parsePageForElementsLinks(scrappingRequest.getUrl());
            List<FilelinkModel> files = getLinksFromElements(elements);
            downloader.downloadFiles(files, destDir);
            downloader.shutdown();
        } catch (IOException e) {
            throw new WebsiteConnectionException("An error occurred while connecting to the website: " + scrappingRequest.getUrl(),
                    e);
        }
    }
    
    private boolean hasMatchingText(Element element, String fileName) {
        return element.text().toLowerCase().contains(fileName.toLowerCase());
    }
    
    private boolean hasValidExtension(Element element, FileType fileType) {
        return element.attr("abs:href").toLowerCase().endsWith(fileType.getExtension().toLowerCase());
    }
    
}