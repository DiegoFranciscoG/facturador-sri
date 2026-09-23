package com.facturadorsri.backend.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Forma unica de error devuelta por la API. Nunca incluye stack traces ni
 * detalles internos (Regla maestra seccion 3: "manejador global de errores
 * sin stack traces").
 */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String mensaje,
        String path,
        List<String> detalles
) {
    public static ApiError de(int status, String error, String mensaje, String path) {
        return new ApiError(LocalDateTime.now(), status, error, mensaje, path, List.of());
    }

    public static ApiError conDetalles(int status, String error, String mensaje, String path, List<String> detalles) {
        return new ApiError(LocalDateTime.now(), status, error, mensaje, path, detalles);
    }
}
