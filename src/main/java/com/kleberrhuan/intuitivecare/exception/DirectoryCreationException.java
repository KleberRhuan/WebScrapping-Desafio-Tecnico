package com.kleberrhuan.intuitivecare.exception;

/**
 * Exceção lançada quando ocorre um erro durante a criação de um diretório.
 */
public class DirectoryCreationException extends RuntimeException {
    public DirectoryCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
