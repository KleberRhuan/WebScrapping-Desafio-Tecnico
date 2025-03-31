package com.kleberrhuan.intuitivecare.exception;

/**
 * Exceção lançada quando ocorre um erro de conexão com o site.
 */
public class WebsiteConnectionException extends RuntimeException {
    public WebsiteConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
