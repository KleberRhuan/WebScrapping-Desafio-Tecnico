package com.kleberrhuan.intuitivecare.exception;

/**
 * Exceção lançada quando ocorre um erro durante o download de um arquivo.
 */
public class FileDownloadException extends RuntimeException {
    public FileDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
