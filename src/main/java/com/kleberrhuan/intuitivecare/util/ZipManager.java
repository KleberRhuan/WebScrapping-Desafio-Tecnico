package com.kleberrhuan.intuitivecare.util;

import com.kleberrhuan.intuitivecare.exception.ZipException;
import com.kleberrhuan.intuitivecare.util.helpers.DirectoryHelper;
import com.kleberrhuan.intuitivecare.util.interfaces.Archiver;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Classe utilitária que implementa a interface Archiver para compactar arquivos
 * em um arquivo ZIP.
 */
public class ZipManager implements Archiver {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ZipManager.class);

    /**
     * Compacta uma lista de arquivos em um arquivo ZIP.
     *
     * @param files       Lista de caminhos de arquivos a serem compactados.
     * @param outputDir   O caminho onde o arquivo ZIP será criado.
     * @param archiveName O nome do arquivo (usado como parte do nome do arquivo
     *                    ZIP).
     */
    @Override
    public void archiveFiles(List<Path> files,
            @NotNull Path outputDir,
            @NotBlank String archiveName) {
        Path archivePath = outputDir.resolve(DirectoryHelper.getZipFileName(archiveName));
        try (ZipOutputStream zipOut = createZipOutputStream(archivePath)) {
            for (Path file : files) {
                addFileToZip(file, zipOut);
            }
        } catch (Exception e) {
            throw new ZipException("Ocorreu um erro ao criar o arquivo ZIP: " + archivePath, e);
        }
    }

    /**
     * Cria um ZipOutputStream para escrever o arquivo ZIP.
     *
     * @param archivePath O caminho para o arquivo ZIP.
     * @return Um ZipOutputStream encapsulando o fluxo de saída do canal.
     * @throws IOException Se ocorrer um erro de I/O ao abrir o canal.
     */
    private ZipOutputStream createZipOutputStream(Path archivePath) throws IOException {
        SeekableByteChannel outChannel = Files.newByteChannel(
                archivePath,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
        return new ZipOutputStream(Channels.newOutputStream(outChannel));
    }

    /**
     * Adiciona um único arquivo ao arquivo ZIP.
     *
     * @param file   O caminho do arquivo a ser adicionado.
     * @param zipOut O ZipOutputStream para escrever o arquivo.
     * @throws IOException Se ocorrer um erro durante a E/S do arquivo.
     */
    private void addFileToZip(Path file, ZipOutputStream zipOut) throws IOException {
        if (Files.notExists(file)) {
            LOGGER.warn("Ignorando arquivo inexistente: {}", file);
            return;
        }
        ZipEntry zipEntry = createZipEntry(file);
        zipOut.putNextEntry(zipEntry);
        copyFileToZip(file, zipOut);
        zipOut.closeEntry();
        LOGGER.info("Adicionado ao ZIP: {}", file);
    }

    /**
     * Cria uma entrada ZIP para um arquivo usando seu nome.
     *
     * @param file O arquivo para o qual criar uma entrada ZIP.
     * @return Uma nova entrada ZIP com o nome do arquivo.
     */
    private ZipEntry createZipEntry(Path file) {
        String fileNameInsideZip = file.getFileName().toString();
        return new ZipEntry(fileNameInsideZip);
    }

    /**
     * Copia o conteúdo do arquivo para o fluxo de saída ZIP usando um
     * BufferedInputStream.
     *
     * @param file   O arquivo a ser copiado para o arquivo ZIP.
     * @param zipOut O ZipOutputStream para escrever os dados do arquivo.
     * @throws IOException Se ocorrer um erro durante a leitura ou escrita do
     *                     arquivo.
     */
    private void copyFileToZip(Path file, ZipOutputStream zipOut) throws IOException {
        try (SeekableByteChannel inChannel = Files.newByteChannel(file, StandardOpenOption.READ);
                BufferedInputStream in = new BufferedInputStream(Channels.newInputStream(inChannel))) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                zipOut.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * Extrai o conteúdo de um arquivo ZIP para o diretório de destino especificado.
     *
     * @param zipFilePath O caminho para o arquivo ZIP.
     * @param outputDir   O diretório onde os arquivos serão extraídos.
     * @throws IOException Se ocorrer um erro durante a extração.
     */
    public static void extractZip(Path zipFilePath, Path outputDir) throws IOException {
        DirectoryHelper.createDirectoryIfNotExists(outputDir);

        try (BufferedInputStream fis = new BufferedInputStream(Files.newInputStream(zipFilePath));
                ZipInputStream zis = new ZipInputStream(fis)) {

            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                Path extractedPath = outputDir.resolve(zipEntry.getName());

                if (zipEntry.isDirectory()) {
                    Files.createDirectories(extractedPath);
                } else {
                    Files.createDirectories(extractedPath.getParent());
                    Files.copy(zis, extractedPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }

        } catch (IOException e) {
            throw new ZipException("Falha ao extrair arquivo ZIP: " + zipFilePath, e);
        }
    }
}