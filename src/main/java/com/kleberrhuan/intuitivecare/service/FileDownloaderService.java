package com.kleberrhuan.intuitivecare.service;

import com.kleberrhuan.intuitivecare.model.FilelinkModel;
import com.kleberrhuan.intuitivecare.util.HttpDownloader;
import com.kleberrhuan.intuitivecare.util.ZipManager;
import com.kleberrhuan.intuitivecare.util.helpers.DirectoryHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Serviço responsável pelo download de arquivos a partir da URL base
 * especificada.
 */
@RequiredArgsConstructor
@Getter
public class FileDownloaderService {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(FileDownloaderService.class);
    private final String baseUrl;
    private final HttpDownloader httpDownloader;

    /**
     * Realiza o download de arquivos das pastas dos N anos mais recentes.
     *
     * @param numberOfYears número de anos recentes para download
     * @throws IOException se houver um erro ao acessar ou criar diretórios
     */
    public void downloadLatestYears(int numberOfYears, Path outputDir) throws IOException {
        List<String> allFolders = retrieveYearFolders();
        allFolders.sort(Comparator.reverseOrder());

        int limit = Math.min(numberOfYears, allFolders.size());
        List<String> latestFolders = allFolders.subList(0, limit);
        for (String folder : latestFolders) {
            String folderUrl = baseUrl + folder + "/";
            downloadAllZipsInAPath(folderUrl, outputDir.resolve(folder));
        }
    }

    /**
     * Recupera as pastas de anos da URL base configurada.
     *
     * @return uma lista com os nomes das pastas de anos
     * @throws IOException se houver um erro ao conectar ou analisar a página remota
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
     * Localiza todos os arquivos zip em uma determinada URL de pasta e faz o
     * download em paralelo.
     *
     * @param url       URL dos arquivos zip
     * @param outputDir diretório local para salvar os downloads
     * @throws IOException se houver um erro ao criar diretórios ou conectar à URL
     *                     da pasta
     */
    private void downloadAllZipsInAPath(String url, Path outputDir) throws IOException {
        LOGGER.info("Acessando pasta: {}", url);

        Document doc = Jsoup.connect(url).get();
        Elements links = doc.select("a[href]");
        Path finalOutputDir = DirectoryHelper.createDirectoryIfNotExists(outputDir);

        links.parallelStream().forEach(link -> {
            String fileHref = link.attr("href");
            if (fileHref.endsWith(".zip")) {
                String fileUrl = url + fileHref;
                LOGGER.info("Baixando arquivo: {}", fileUrl);
                try {
                    FilelinkModel fileLink = new FilelinkModel(fileHref, fileUrl);
                    httpDownloader.downloadFile(fileLink, finalOutputDir);
                    ZipManager.extractZip(finalOutputDir.resolve(fileHref), finalOutputDir);
                } catch (IOException e) {
                    LOGGER.error("Falha ao baixar arquivo: {}", fileUrl, e);
                }
            }
        });
    }
}