package com.kleberrhuan.intuitivecare.cli;

import com.kleberrhuan.intuitivecare.config.AppConfig;
import com.kleberrhuan.intuitivecare.model.FileModel;
import com.kleberrhuan.intuitivecare.model.FileType;
import com.kleberrhuan.intuitivecare.model.ScrappingRequest;
import com.kleberrhuan.intuitivecare.service.FileDownloaderService;
import com.kleberrhuan.intuitivecare.service.PdfProcessingService;
import com.kleberrhuan.intuitivecare.service.ScrapperService;
import com.kleberrhuan.intuitivecare.util.HttpDownloader;
import com.kleberrhuan.intuitivecare.util.ZipManager;
import com.kleberrhuan.intuitivecare.util.helpers.DirectoryHelper;
import com.kleberrhuan.intuitivecare.util.helpers.ScannerHelper;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Gerenciador da interface de linha de comando para o projeto IntuitiveCare
 * Implementa os requisitos 1 e 2 do teste
 */
public class CliManager {
  private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(CliManager.class);

  private final ScannerHelper scannerHelper;

  public CliManager(ScannerHelper scannerHelper) {
    this.scannerHelper = scannerHelper;
  }

  /**
   * Executa o fluxo principal do programa
   */
  public void run() {
    LOGGER.info("Iniciando execução do programa IntuitiveCare");
    showMenu();
    int option = scannerHelper.nextInt();

    switch (option) {
      case 1 -> executeWebScraping();
      case 2 -> executeDataTransformation();
      case 3 -> executeCompleteFlow();
      case 4 -> executeAccountingStatementsDownload();
      default -> LOGGER.warn("Opção inválida. Encerrando programa.");
    }

    LOGGER.info("Programa finalizado");
  }

  private void showMenu() {
    LOGGER.info("==== IntuitiveCare - Teste Técnico ====");
    LOGGER.info("1. Executar Web Scraping (Requisito 1)");
    LOGGER.info("2. Executar Transformação de Dados (Requisito 2)");
    LOGGER.info("3. Executar Fluxo Completo (Requisitos 1 + 2)");
    LOGGER.info("4. Download de Demonstrações Contábeis ANS(Ultimos 2 anos)");
    LOGGER.info("Digite a opção desejada: ");
  }

  /**
   * Implementa o requisito 1: Web Scraping
   * 1.1. Acesso ao site da ANS
   * 1.2. Download dos Anexos I e II em formato PDF
   * 1.3. Compactação de todos os anexos em um único arquivo
   */
  private void executeWebScraping() {
    LOGGER.info("Iniciando web scraping no site da ANS: {}", AppConfig.ANS_URL);

    try {
      Path outputDir = DirectoryHelper.createDirectoryIfNotExists(AppConfig.OUTPUT_DIR);

      // 1.1 e 1.2 - Acessar site e baixar os anexos I e II
      List<FileModel> targetFiles = Arrays.asList(
          new FileModel(AppConfig.ANEXO_I, FileType.PDF),
          new FileModel(AppConfig.ANEXO_II, FileType.PDF));

      ScrappingRequest request = ScrappingRequest.builder()
          .url(AppConfig.ANS_URL)
          .files(targetFiles)
          .build();

      ScrapperService scrapper = new ScrapperService(request);
      scrapper.downloadFiles(AppConfig.OUTPUT_DIR);
      LOGGER.info("Arquivos PDF baixados com sucesso para: {}", AppConfig.OUTPUT_DIR);

      // 1.3 - Compactar os anexos em um único arquivo ZIP
      List<Path> pdfsToCompress = targetFiles.stream()
          .map(fileModel -> AppConfig.OUTPUT_DIR.resolve(fileModel.getFullName()))
          .toList();

      ZipManager zipManager = new ZipManager();
      zipManager.archiveFiles(pdfsToCompress, outputDir, AppConfig.ZIP_FILENAME);
      LOGGER.info("Arquivos compactados com sucesso em: {}/{}", AppConfig.OUTPUT_DIR, AppConfig.ZIP_FILENAME);

    } catch (Exception e) {
      LOGGER.error("Erro ao executar web scraping: {}", e.getMessage(), e);
    }
  }

  /**
   * Implementa o requisito 2: Transformação de Dados
   * 2.1. Extrair dados da tabela do PDF do Anexo I
   * 2.2. Salvar dados em formato CSV
   * 2.3. Compactar o CSV
   * 2.4. Substituir abreviações (implementado no PdfProcessingService)
   */
  private void executeDataTransformation() {
    LOGGER.info("Iniciando transformação de dados do PDF");

    try {
      Path pdfPath = AppConfig.OUTPUT_DIR.resolve(AppConfig.DEFAULT_PDF_FILENAME);
      Path csvPath = AppConfig.OUTPUT_DIR.resolve(AppConfig.DEFAULT_CSV_FILENAME);
      Path zipPath = AppConfig.OUTPUT_DIR.resolve(AppConfig.DEFAULT_ZIP_FILENAME);

      if (!pdfPath.toFile().exists()) {
        LOGGER.error("Arquivo PDF não encontrado em: {}", pdfPath);
        LOGGER.warn("Arquivo PDF não encontrado. Execute primeiro o web scraping.");
        return;
      }

      // Executar processamento do PDF (extração, conversão para CSV e compactação)
      PdfProcessingService pdfProcessor = new PdfProcessingService();
      String resultado = pdfProcessor.processPdf(pdfPath, csvPath, zipPath);

      LOGGER.info("Transformação de dados concluída: {}", resultado);

    } catch (Exception e) {
      LOGGER.error("Erro ao transformar dados: {}", e.getMessage(), e);
    }
  }

  /**
   * Executa o fluxo completo: Web Scraping + Transformação de Dados
   */
  private void executeCompleteFlow() {
    LOGGER.info("Iniciando fluxo completo (Web Scraping + Transformação de Dados)");
    executeWebScraping();
    executeDataTransformation();
    LOGGER.info("Fluxo completo finalizado com sucesso");
  }

  /**
   * Executa o download das demonstrações contábeis da ANS
   * Faz download de arquivos das pastas dos últimos anos e extrai o conteúdo.
   */
  private void executeAccountingStatementsDownload() {
    LOGGER.info("Iniciando download de demonstrações contábeis da ANS");

    try {
      LOGGER.info("Quantos anos de dados deseja baixar? (Recomendado: 2)");
      int numberOfYears = scannerHelper.nextInt();

      if (numberOfYears <= 0) {
        LOGGER.warn("Número de anos deve ser maior que zero. Usando o valor padrão de 2 anos.");
        numberOfYears = 2;
      }

      FileDownloaderService service = new FileDownloaderService(
          AppConfig.ANS_DEMONSTRACOES_URL,
          new HttpDownloader(AppConfig.DOWNLOAD_THREADS));

      LOGGER.info("Baixando dados dos últimos {} anos", numberOfYears);
      service.downloadLatestYears(numberOfYears, AppConfig.OUTPUT_DIR);
      LOGGER.info("Download das demonstrações contábeis concluído com sucesso");

    } catch (Exception e) {
      LOGGER.error("Erro ao baixar demonstrações contábeis: {}", e.getMessage(), e);
    }
  }
}