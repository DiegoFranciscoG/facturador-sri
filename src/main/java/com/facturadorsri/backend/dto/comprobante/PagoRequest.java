package com.facturadorsri.backend.dto.comprobante;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PagoRequest(
        @NotBlank String formaPago,

        @NotNull
        @DecimalMin(value = "0.01", message = "el total del pago debe ser mayor a cero")
        @DecimalMax(value = "9999999999.99", message = "el total es demasiado alto")
        @Digits(integer = 12, fraction = 2, message = "maximo 2 decimales")
        BigDecimal total,

        @Positive(message = "el plazo debe ser un numero positivo")
        @Max(value = 999, message = "plazo demasiado alto")
        Integer plazo,

        @Size(max = 20)
        String unidadTiempo
) {
}
