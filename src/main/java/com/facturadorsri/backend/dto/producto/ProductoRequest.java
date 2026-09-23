package com.facturadorsri.backend.dto.producto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductoRequest(
        @NotBlank @Size(max = 25)
        String codigoPrincipal,

        @Size(max = 25)
        String codigoAuxiliar,

        @NotBlank @Size(max = 300)
        String descripcion,

        @NotBlank @Size(max = 20)
        String unidadMedida,

        @NotNull @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal precioUnitario,

        @NotBlank @Pattern(regexp = "2|3|5", message = "codigo de impuesto no valido")
        String codigoImpuesto,

        @NotBlank
        String codigoPorcentajeIva
) {
}
