package com.facturadorsri.backend.dto.comprobante;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PagoRequest(
        @NotBlank String formaPago,
        @NotNull @DecimalMin(value = "0.0") BigDecimal total,
        Integer plazo,
        String unidadTiempo
) {
}
