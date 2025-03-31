package com.kleberrhuan.intuitivecare.service;

import com.kleberrhuan.intuitivecare.config.AppConfig;
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
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço responsável por realizar web scraping e download de arquivos
 */
@RequiredArgsConstructor
public class ScrapperService {
    private final ScrappingRequest scrappingRequest;
    private static final HttpDownloader downloader;

    static {
        downloader = new HttpDownloader(AppConfig.DOWNLOAD_THREADS);
    }

    /**
     * Conecta-se à URL fornecida usando Jsoup e extrai elementos que contêm uma das
     * extensões
     * de arquivo especificadas na ScrappingRequest.
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
     * Percorre cada definição de arquivo (da ScrappingRequest) e tenta encontrar
     * um elemento cujo texto inclua o nome do arquivo e tenha a extensão de tipo de
     * arquivo correta.
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
                                .orElse(null)))
                .toList();
    }

    /**
     * Realiza o download dos arquivos para o diretório de destino especificado
     * 
     * @param outputDir diretório de destino para salvar os arquivos
     * @throws WebsiteConnectionException se ocorrer um erro ao conectar ao site
     */
    public void downloadFiles(Path outputDir) throws WebsiteConnectionException {
        try {
            Elements elements = parsePageForElementsLinks(scrappingRequest.getUrl());
            List<FilelinkModel> files = getLinksFromElements(elements);
            downloader.downloadFiles(files, outputDir);
            downloader.shutdown();
        } catch (IOException e) {
            throw new WebsiteConnectionException("Ocorreu um erro ao conectar ao site: " + scrappingRequest.getUrl(),
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