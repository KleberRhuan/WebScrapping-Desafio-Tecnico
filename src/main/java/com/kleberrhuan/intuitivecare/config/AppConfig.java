package com.kleberrhuan.intuitivecare.config;

import java.nio.file.Path;

/**
 * Centraliza as configurações e constantes da aplicação
 */
public class AppConfig {

  public static final String ANEXO_I = "Anexo I";
  public static final String ANEXO_II = "Anexo II";

  public static final String DEFAULT_PDF_FILENAME = "Anexo_I.pdf";
  public static final String DEFAULT_CSV_FILENAME = "Anexo_I.csv";
  public static final String DEFAULT_ZIP_FILENAME = "Teste_Kleber_Rhuan.zip";

  /** URL do site da ANS para web scraping */
  public static final String ANS_URL = "https://www.gov.br/ans/pt-br/acesso-a-informacao/participacao-da-sociedade/atualizacao-do-rol-de-procedimentos";

  /** URL do site da ANS para demonstrações contábeis */
  public static final String ANS_DEMONSTRACOES_URL = "https://dadosabertos.ans.gov.br/FTP/PDA/demonstracoes_contabeis/";

  /** Diretório de saída para os arquivos */
  public static final Path OUTPUT_DIR = Path.of("output");

  /** Nome do arquivo ZIP de saída */
  public static final String ZIP_FILENAME = "Teste_Kleber_Rhuan.zip";

  /** Tempo de timeout para conexões em milissegundos */
  public static final int CONNECTION_TIMEOUT = 30000;

  /** Número de threads para download paralelo */
  public static final int DOWNLOAD_THREADS = 4;

  /** Mapeamento de abreviações para substituição */
  public static final String[][] ABBREVIATION_MAPPING = {
      { "OD", "Seg. Odontológica" },
      { "AMB", "Seg. Ambulatorial" }
  };

  private AppConfig() {
    throw new AssertionError("Esta classe não deve ser instanciada");
  }
}