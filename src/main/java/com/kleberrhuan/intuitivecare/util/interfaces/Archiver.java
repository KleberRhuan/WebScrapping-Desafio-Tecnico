package com.kleberrhuan.intuitivecare.util.interfaces;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Interface para classes que realizam operações de compactação de arquivos.
 */
public interface Archiver {
    /**
     * Compacta uma lista de arquivos.
     *
     * @param files       Lista de caminhos de arquivos a serem compactados
     * @param outputDir     Diretório de destino para o arquivo compactado
     * @param archiveName Nome do arquivo compactado
     * @throws IOException Se ocorrer um erro durante o processo de compactação
     */
    void archiveFiles(List<Path> files, Path outputDir, String archiveName) throws IOException;
}
