package com.facturadorsri.backend.dto.producto;

import java.math.BigDecimal;

public record ProductoResponse(
        Long id,
        String codigoPrincipal,
        String codigoAuxiliar,
        String descripcion,
        String unidadMedida,
        BigDecimal precioUnitario,
        String codigoImpuesto,
        String codigoPorcentajeIva,
        boolean activo
) {
}
