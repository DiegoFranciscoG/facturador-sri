package com.facturadorsri.backend.service.sri.soap;

import java.util.List;

/** RECIBIDA: paso la validacion de esquema/clave de acceso. DEVUELTA: rechazada. */
public record RespuestaRecepcion(String estado, List<MensajeSri> mensajes) {

    public boolean fueRecibida() {
        return "RECIBIDA".equalsIgnoreCase(estado);
    }
}
