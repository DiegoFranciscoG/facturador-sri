package com.facturadorsri.backend.dto.producto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductoRequest(
        @NotBlank @Size(max = 25) @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "solo letras, numeros, punto y guion")
        String codigoPrincipal,

        @Size(max = 25) @Pattern(regexp = "^$|^[A-Za-z0-9._-]+$", message = "solo letras, numeros, punto y guion")
        String codigoAuxiliar,

        @NotBlank @Size(max = 300)
        String descripcion,

        @NotBlank @Size(max = 20)
        String unidadMedida,

        @NotNull
        @DecimalMin(value = "0.01", message = "el precio debe ser mayor a cero")
        @DecimalMax(value = "999999.999999", message = "el precio es demasiado alto")
        @Digits(integer = 6, fraction = 6, message = "maximo 6 decimales")
        BigDecimal precioUnitario,

        @NotBlank @Pattern(regexp = "2|3|5", message = "codigo de impuesto no valido")
        String codigoImpuesto,

        @NotBlank
        String codigoPorcentajeIva
) {
}
