package com.facturadorsri.backend.exception;

/** Fallo de comunicacion con los web services del SRI (o su simulador). */
public class SriIntegrationException extends RuntimeException {

    public SriIntegrationException(String mensaje) {
        super(mensaje);
    }

    public SriIntegrationException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
