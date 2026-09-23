package com.facturadorsri.backend.domain.enums;

/**
 * Ciclo de vida de un comprobante en el esquema offline (docs/investigacion.md 2 y 7).
 * GENERADO -> FIRMADO -> RECIBIDO -> (DEVUELTA | AUTORIZADO | NO_AUTORIZADO)
 * Desde DEVUELTA se puede reintentar el envio (vuelve a FIRMADO).
 */
public enum EstadoComprobante {
    GENERADO,
    FIRMADO,
    RECIBIDO,
    DEVUELTA,
    AUTORIZADO,
    NO_AUTORIZADO
}
