package com.facturadorsri.backend.dto.cliente;

public record ClienteResponse(
        Long id,
        String tipoIdentificacion,
        String identificacion,
        String razonSocial,
        String direccion,
        String email,
        String telefono
) {
}
