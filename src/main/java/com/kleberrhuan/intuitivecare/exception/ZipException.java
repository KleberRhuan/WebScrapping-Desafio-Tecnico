package com.kleberrhuan.intuitivecare.exception;

/**
 * Exceção específica para erros relacionados a operações com arquivos ZIP.
 */
public class ZipException extends RuntimeException {
    public ZipException(String message, Throwable cause) {
        super(message, cause);
    }
}
