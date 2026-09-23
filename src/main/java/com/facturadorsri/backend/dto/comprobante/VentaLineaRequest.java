package com.facturadorsri.backend.dto.comprobante;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record VentaLineaRequest(
        @NotNull Long productoId,
        @NotNull @DecimalMin(value = "0.000001") BigDecimal cantidad,
        @DecimalMin(value = "0.0") BigDecimal descuento
) {
}
