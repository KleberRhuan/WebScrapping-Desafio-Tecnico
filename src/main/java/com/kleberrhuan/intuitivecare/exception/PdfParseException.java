package com.kleberrhuan.intuitivecare.exception;

/**
 * Exceção lançada quando ocorre um erro durante o processamento ou análise de
 * um arquivo PDF.
 */
public class PdfParseException extends RuntimeException {
    public PdfParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
