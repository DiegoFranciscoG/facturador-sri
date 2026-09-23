package com.facturadorsri.backend.exception;

/** Violacion de una regla de negocio (no es un error de validacion de campos). */
public class BusinessException extends RuntimeException {

    public BusinessException(String mensaje) {
        super(mensaje);
    }

    public BusinessException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
