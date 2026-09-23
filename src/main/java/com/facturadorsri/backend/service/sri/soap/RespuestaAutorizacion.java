package com.facturadorsri.backend.service.sri.soap;

import java.util.List;

/** AUTORIZADO, NO AUTORIZADO, o EN PROCESAMIENTO (hay que reintentar la consulta). */
public record RespuestaAutorizacion(
        String estado,
        String numeroAutorizacion,
        String fechaAutorizacion,
        String comprobanteAutorizadoXml,
        List<MensajeSri> mensajes
) {
    public boolean estaAutorizado() {
        return "AUTORIZADO".equalsIgnoreCase(estado);
    }

    public boolean estaEnProcesamiento() {
        return "EN PROCESAMIENTO".equalsIgnoreCase(estado);
    }
}
