package com.facturadorsri.backend.exception;

public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String mensaje) {
        super(mensaje);
    }
}
