package com.kleberrhuan.intuitivecare.util.interfaces;

import com.kleberrhuan.intuitivecare.model.FilelinkModel;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Interface para classes que realizam operações de download de arquivos.
 */
public interface Downloader {
    /**
     * Faz uma requisição HTTP para baixar um arquivo da URL fornecida e salvá-lo no
     * caminho especificado.
     *
     * @param file           objeto contendo a URL e o nome do arquivo a ser
     *                       baixado.
     * @param destinationDir Diretório local onde o arquivo será salvo.
     * @throws IOException Se ocorrer um erro durante o download.
     */
    void downloadFile(FilelinkModel file, Path outputDir) throws IOException;
}
