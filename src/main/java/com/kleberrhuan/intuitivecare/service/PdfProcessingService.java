package com.kleberrhuan.intuitivecare.service;

import com.kleberrhuan.intuitivecare.config.AppConfig;
import com.kleberrhuan.intuitivecare.exception.PdfParseException;
import com.kleberrhuan.intuitivecare.util.interfaces.Archiver;
import com.kleberrhuan.intuitivecare.util.ZipManager;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Serviço responsável pelo processamento de PDFs, extração de tabelas e
 * conversão para formato CSV
 */
public class PdfProcessingService {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(PdfProcessingService.class);

    private final Map<String, String> abbreviationMap;
    private final Archiver archiver = new ZipManager();

    /**
     * Construtor padrão que inicializa o mapeamento de abreviações a partir da
     * configuração
     */
    public PdfProcessingService() {
        abbreviationMap = new HashMap<>();
        for (String[] mapping : AppConfig.ABBREVIATION_MAPPING) {
            abbreviationMap.put(mapping[0], mapping[1]);
        }
    }

    /**
     * Orquestra o pipeline completo de processamento de PDF: extração de tabelas,
     * aplicação de mapeamento de abreviações, geração de arquivo CSV e compactação.
     *
     * @param pdfPath   Caminho para o arquivo PDF
     * @param csvOutput Caminho para o arquivo CSV gerado
     * @param zipOutput Caminho para o arquivo ZIP
     * @return Uma mensagem indicando sucesso ou falha
     */
    public String processPdf(Path pdfPath, Path csvOutput, Path zipOutput) {
        try {
            LOGGER.info("Iniciando processamento do PDF: {}", pdfPath);
            List<List<String>> rows = extractTabularData(pdfPath);
            LOGGER.info("Dados extraídos do PDF: {} linhas encontradas", rows.size());

            applyAbbreviations(rows);
            LOGGER.info("Abreviações substituídas com sucesso");

            writeCsv(rows, csvOutput);
            LOGGER.info("Arquivo CSV gerado: {}", csvOutput);
            ;
            archiver.archiveFiles(
                    List.of(csvOutput),
                    zipOutput.getParent(),
                    zipOutput.getFileName().toString());
            LOGGER.info("Arquivo compactado gerado: {}", zipOutput);

            return "Processo concluído com sucesso";
        } catch (IOException e) {
            LOGGER.error("Erro ao processar PDF: {}", e.getMessage(), e);
            throw new PdfParseException("Erro no processamento do PDF", e);
        }
    }

    /**
     * Extrai dados tabulares estruturados de todas as páginas do PDF.
     *
     * @param pdfPath Caminho para o arquivo PDF
     * @return Uma lista de linhas com valores de células
     * @throws IOException Se a leitura ou análise do PDF falhar
     */
    private List<List<String>> extractTabularData(Path pdfPath) throws IOException {
        List<List<String>> allRows = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(pdfPath.toFile());
                ObjectExtractor extractor = new ObjectExtractor(document)) {

            SpreadsheetExtractionAlgorithm algorithm = new SpreadsheetExtractionAlgorithm();
            int totalPages = document.getNumberOfPages();
            List<String> header = null;

            for (int i = 0; i < totalPages; i++) {
                List<List<String>> pageRows = extractRowsFromPage(extractor, algorithm, i + 1);
                for (List<String> cells : pageRows) {
                    if (!cells.isEmpty() && !allCellsBlank(cells)) {
                        if (header == null) {
                            header = new ArrayList<>(cells);
                            allRows.add(header);
                        } else if (!cells.equals(header)) {
                            allRows.add(cells);
                        }
                    }
                }
            }
        }
        return allRows;
    }

    private boolean allCellsBlank(List<String> cells) {
        return cells.stream().allMatch(String::isBlank);
    }

    private List<List<String>> extractRowsFromPage(ObjectExtractor extractor,
            SpreadsheetExtractionAlgorithm algorithm,
            int pageNumber) {
        List<List<String>> rows = new ArrayList<>();
        Page page = extractor.extract(pageNumber);
        List<Table> tables = algorithm.extract(page);

        for (Table table : tables) {
            for (List<RectangularTextContainer> row : table.getRows()) {
                rows.add(extractCellsFromRow(row));
            }
        }

        return rows;
    }

    private List<String> extractCellsFromRow(List<RectangularTextContainer> row) {
        return row.stream()
                .map(RectangularTextContainer::getText)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Substitui abreviações específicas nas linhas extraídas usando o mapa
     * predefinido.
     *
     * @param rows Lista de linhas de tabela com texto de célula
     */
    private void applyAbbreviations(List<List<String>> rows) {
        for (List<String> row : rows) {
            for (int i = 0; i < row.size(); i++) {
                String cell = row.get(i);
                if (cell != null) {
                    String match = abbreviationMap.get(cell.toUpperCase());
                    if (match != null) {
                        row.set(i, match);
                    }
                }
            }
        }
    }

    /**
     * Escreve os dados processados em um arquivo CSV usando valores separados por
     * vírgula e entre aspas.
     *
     * @param rows    As linhas de dados para escrever
     * @param csvPath Caminho de saída para o arquivo CSV
     * @throws IOException Se falhar ao escrever no arquivo
     */
    private void writeCsv(List<List<String>> rows, Path csvPath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(csvPath.toFile(), false))) {
            for (List<String> row : rows) {
                String line = row.stream()
                        .map(s -> s.replace("\"", "\"\""))
                        .map(s -> "\"" + s + "\"")
                        .collect(Collectors.joining(","));
                writer.println(line);
            }
        }
    }

    /**
     * Ponto de entrada para testes manuais e desenvolvimento.
     */
    public static void main(String[] args) {
        PdfProcessingService service = new PdfProcessingService();
        Path pdfPath = AppConfig.OUTPUT_DIR.resolve("Anexo_I.pdf");
        Path csvPath = AppConfig.OUTPUT_DIR.resolve("output.csv");
        Path zipPath = AppConfig.OUTPUT_DIR.resolve("output.zip");
        service.processPdf(pdfPath, csvPath, zipPath);
    }
}