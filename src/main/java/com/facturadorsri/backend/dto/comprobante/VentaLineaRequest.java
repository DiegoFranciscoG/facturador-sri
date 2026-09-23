package com.facturadorsri.backend.dto.comprobante;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record VentaLineaRequest(
        @NotNull Long productoId,

        @NotNull
        @DecimalMin(value = "0.000001", message = "la cantidad debe ser mayor a cero")
        @DecimalMax(value = "999999.999999", message = "la cantidad es demasiado alta")
        @Digits(integer = 6, fraction = 6, message = "maximo 6 decimales")
        BigDecimal cantidad,

        @DecimalMin(value = "0.0", message = "el descuento no puede ser negativo")
        @DecimalMax(value = "999999.99", message = "el descuento es demasiado alto")
        @Digits(integer = 12, fraction = 2, message = "maximo 2 decimales")
        BigDecimal descuento
) {
}
