package com.kleberrhuan.intuitivecare.util.helpers;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Classe utilitária para operações com diretórios.
 * Esta classe fornece métodos para validar e criar diretórios, garantindo que o
 * caminho informado seja válido.
 */
public final class DirectoryHelper {
    private DirectoryHelper() {
    }

    /**
     * Cria o diretório especificado se ele não existir.
     * Se o caminho existir mas não for um diretório, uma IllegalArgumentException
     * será lançada.
     *
     * @param dir o caminho do diretório a ser criado.
     * @return o caminho do diretório existente ou recém-criado.
     * @throws IOException              se ocorrer um erro de E/S durante a criação.
     * @throws IllegalArgumentException se o caminho fornecido for nulo ou existir e
     *                                  não for um diretório.
     */
    public static Path createDirectoryIfNotExists(@NotNull Path dir) throws IOException {
        boolean exists = Files.exists(dir);
        if (exists && !Files.isDirectory(dir)) {
            throw new IllegalArgumentException("O caminho " + dir + " existe, mas não é um diretório.");
        } else if (!exists) {
            Files.createDirectories(dir);
        }
        return dir;
    }

    /**
     * Cria o diretório especificado a partir de uma String se ele não existir.
     *
     * @param directoryPath o caminho do diretório como uma String.
     * @return o caminho do diretório existente ou recém-criado.
     * @throws IOException              se ocorrer um erro de E/S durante a criação.
     * @throws IllegalArgumentException se o caminho fornecido for nulo ou vazio.
     */
    public static Path createDirectoryIfNotExists(String directoryPath) throws IOException {
        if (directoryPath == null || directoryPath.isEmpty()) {
            throw new IllegalArgumentException("O caminho do diretório não pode ser nulo ou vazio.");
        }
        Path dir = Paths.get(directoryPath);
        return createDirectoryIfNotExists(dir);
    }

    /**
     * Obtém o nome do arquivo ZIP, adicionando a extensão .zip se necessário.
     *
     * @param fileName o nome do arquivo
     * @return o nome do arquivo com a extensão .zip
     */
    public static String getZipFileName(@NotBlank String fileName) {
        if (fileName.toLowerCase().endsWith(".zip")) {
            return fileName;
        } else {
            return fileName + ".zip";
        }
    }
}